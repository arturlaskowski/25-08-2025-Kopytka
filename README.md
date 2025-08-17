# Saga Pattern - Kopytka

Ten branch pokazuje przykładową implementację wzorca **Saga Pattern**.  
Czyli jedną transakcję biznesową, która dzieli się na kilka transakcji bazodanowych.  
Skutkuje to wykonanie wielu zmian w rozproszonym środowisku (na różnych bazach danych), wraz z ewentualną kompensacją w razie problemów.

Zaimplementowany tu wariant **Saga Pattern** to **orkiestracja** – czyli istnieje jeden moduł, który zarządza całym flow.  
Ponieważ proces jest stabilny, moduł ten został dodany do mikroserwisu `order-service`, w pakiecie [saga](order-service/src/main/java/pl/kopytka/order/saga).

---

## Flow – Happy Path

1. Przychodzi request do `order-service`: `POST /api/orders` – rozpoczyna się proces składania zamówienia oraz uruchamiana jest saga (`PROCESSING`).
2. W `order-service`, podczas składania zamówienia, wysyłana jest komenda `CreatePaymentCommand`, a zamówienie zapisywane jest ze statusem `PENDING`.
3. Mikroserwis `payment-service` nasłuchuje komendy `CreatePaymentCommand`, wykonuje płatność i wysyła event `PaymentCompletedEvent`.
4. `order-service` nasłuchuje eventu `PaymentCompletedEvent`, zmienia status zamówienia na `PAID`, a następnie wysyła komendę `RestaurantApproveOrderCommand`.
5. Mikroserwis `restaurant-service` nasłuchuje komendy `RestaurantApproveOrderCommand`, zatwierdza zamówienie i wysyła event `RestaurantOrderApprovedEvent`.
6. `order-service` nasłuchuje eventu `RestaurantOrderApprovedEvent`, zmienia status zamówienia na `APPROVED` i kończy sagę (`SUCCEEDED`).

---

## Flow – Kompensacja (gdy coś pójdzie nie tak)

Zdarzają się sytuacje, w których coś się nie udaje – np. płatność zostaje odrzucona, restauracja jest zamknięta, lub odrzuca zamówienie z powodu braku produktu.  
Weźmy scenariusz, w którym zamówienie zostało opłacone, ale restauracja odrzuca je z powodu braku produktu.

1. Przychodzi request do `order-service`: `POST /api/orders` – rozpoczyna się proces składania zamówienia oraz uruchamiana jest saga (`PROCESSING`).
2. `order-service` wysyła komendę `CreatePaymentCommand`, zamówienie zapisuje się ze statusem `PENDING`.
3. `payment-service` nasłuchuje komendy, wykonuje płatność i wysyła event `PaymentCompletedEvent`.
4. `order-service` odbiera event `PaymentCompletedEvent`, zmienia status zamówienia na `PAID` i wysyła komendę `RestaurantApproveOrderCommand`.
5. `restaurant-service` odbiera komendę i **odrzuca** zamówienie, wysyłając event `RestaurantOrderRejectedEvent`.
6. `order-service` odbiera event `RestaurantOrderRejectedEvent`, zmienia status zamówienia na `CANCELLING`, a saga wchodzi w status `COMPENSATING` i wysyła komendę `CancelPaymentCommand`.
7. `payment-service` odbiera komendę `CancelPaymentCommand`, wycofuje płatność i wysyła event `PaymentCancelledEvent`.
8. `order-service` odbiera event `PaymentCancelledEvent`, zmienia status zamówienia na `CANCELLED` i kończy sagę ustawiając status `COMPENSATED`.

---

Dzięki temu podejściu możemy bezpiecznie zarządzać rozproszonymi transakcjami i zapewnić spójność danych pomiędzy mikroserwisami.

Jeśli **Saga** nie zakończy się w wyznaczonym czasie i pozostanie w statusie **PENDING** lub **COMPENSATING**, to powinna przejść do stanu **FAILED** (zob. [SagaTimeoutScheduler](order-service/src/main/java/pl/kopytka/order/saga/SagaTimeoutScheduler.java)).
Co dalej z taką sagą zrobić, zależy już od procesu biznesowego — czy warto ją ponownie przetwarzać, czy konieczna jest automatyzacja, czy też skala problemu jest na tyle mała, że wystarczy, aby ktoś raz dziennie zweryfikował i poprawił ją ręcznie. To już szczegół implementacyjny i zależy od konkretnego procesu :)

# Stawianie środowiska (dla przypomnienia)

* **Uruchom infrastrukturę** za pomocą pliku [docker-compose](infrastructure/docker-compose.yml).

* **GUI do Kafki**
  Po uruchomieniu, pod adresem [http://localhost:8080/](http://localhost:8080/) dostępne jest GUI do Kafki z podłączonym Schema Registry. Możesz tam weryfikować, jakie wiadomości pojawiły się na poszczególnych topicach.

---

### Baza danych

Każda baza danych to osobny schemat. Zapewnia to separację, oszczędza lokalnie zasoby, a w środowisku wdrożeniowym pozwala korzystać z oddzielnych baz danych.
Po zalogowaniu się do bazy jako użytkownik `admin_user` z hasłem `admin_password` (`jdbc:postgresql://localhost:5432/kopytkadb`), masz dostęp do wszystkich schematów.

---

### PgAdmin (opcjonalnie)

Jeśli nie korzystasz z IntelliJ w wersji Ultimate, możesz użyć **pgAdmina** do zarządzania bazą danych.
Aby go uruchomić, użyj pliku [docker-compose.pgadmin.yml](infrastructure/docker-compose.pgadmin.yml).

Po uruchomieniu (po około minucie) będzie dostępny pod adresem:
[http://localhost:5050](http://localhost:5050)

Baza danych **kopytkaDb** powinna być już skonfigurowana.
Jeśli pojawi się okno z prośbą o ustawienie hasła lub danych dostępowych wpisz `postgres`.


### Czyszczenie infrastruktury

Polecenie z pliku [docker-clean.sh](infrastructure/docker-clean.sh) usuwa całą infrastrukturę.
Zwalnia to zasoby i zapobiega kolizjom przy przełączaniu się na projekt **Punktozaur**.

Jeśli stworzyłeś lub zmodyfikowałeś schematy Avro dla wiadomości ([resources-avro](common/src/main/resources/avro)), to usuń katalog
[avro](common/src/main/java/pl/kopytka/avro) i ponownie skompiluj projekt common, np. używając polecenia `mvn compile`.


