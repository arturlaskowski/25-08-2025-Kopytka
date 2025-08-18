## Kolejność operacji: zapis do bazy a wysyłanie wiadomości

Gdy wykonujemy zmianę na zasobie, a następnie chcemy wyemitować zdarzenie informujące o dokonanej zmianie, musimy pamiętać, że **transakcja w Springu zamyka się po wyjściu z metody**, jeśli korzystamy z adnotacji `@Transactional`.

Jeżeli w takiej metodzie wywołamy `kafkaProducer.send`, może się zdarzyć, że wiadomość zostanie wysłana do brokera, ale zapis do bazy danych się nie powiedzie, np. z powodu ograniczeń (`constraint`) lub blokady optymistycznej (`optimistic lock`).

---

## Problem i rozwiązanie

Chcielibyśmy mieć pewność, że próba wysłania wiadomości na brokera nastąpi **dopiero po zatwierdzeniu (commit) transakcji bazodanowej**.

Możemy to osiągnąć na kilka sposobów:

* W przypadku **replikacji (state transfer event)** można oprzeć się na mechanizmie `@EntityListener`. Przykład: [RestaurantEntityListener](restaurant-service/src/main/java/pl/kopytka/restaurant/domain/RestaurantEntityListener.java).
* W przypadku **notification event** warto zadeklarować to jawnie w kodzie. Aby wiadomość została wysłana dopiero po zatwierdzeniu transakcji, można użyć mechanizmu `ApplicationEventPublisher`.

---

## Użycie `ApplicationEventPublisher`

Metodę nasłuchującą na emitowane zdarzenie należy oznaczyć adnotacją:

```
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

Oznacza to, że zdarzenie zostanie przetworzone **dopiero po zatwierdzeniu transakcji** w metodzie, która je wyemitowała.
Domyślnie `@TransactionalEventListener` używa `phase = TransactionPhase.AFTER_COMMIT`, więc jawne deklarowanie `phase` jest opcjonalne.

W Springu może występować wiele zagnieżdżonych transakcji. Samo `phase = TransactionPhase.AFTER_COMMIT` oznacza, że listener wykona się po zakończeniu transakcji, ale jeśli istniała transakcja nadrzędna, może ona wpłynąć na wycofanie listenera.
Aby mieć pewność, że transakcja nadrzędna go nie wycofa, można uruchomić listener w osobnej transakcji, np.:

```
@Transactional(propagation = REQUIRES_NEW)
```

Jednak tworzenie nowej transakcji dla operacji, która nie modyfikuje bazy, może generować nadmierny narzut. Dlatego często stosuje się **@Async**, czyli uruchomienie listenera w osobnym wątku po commicie bazodanowym, niezależnym od transakcji nadrzędnej.
Tak zostało to zrealizowane w tym branchu.

Aby asynchroniczność działała, należy dodać:

```
@EnableAsync
```

do klasy konfiguracyjnej Springa: [Konfiguracja publishera](common/src/main/java/pl/kopytka/common/config/DomainEventPublisherConfiguration.java).

---

Na tym branchu wszystkie **notification event** zostały przerobione na to podejście.

Przykładowo, emitowanie wiadomości odbywa się za pomocą `EventsPublisher`, który korzysta z `ApplicationEventPublisher`:
[PaymentApplicationService](payment-service/src/main/java/pl/kopytka/payment/application/PaymentApplicationService.java)

Przechwytywanie tej wiadomości po zatwierdzeniu transakcji i publikowanie zdarzenia na brokera:
[KafkaPaymentEventPublisher](payment-service/src/main/java/pl/kopytka/payment/messaging/KafkaPaymentEventPublisher.java)
