# Outbox Pattern

## Problem

Podczas procesu biznesowego w architekturze rozproszonej, gdzie emitujemy eventy po wykonaniu zmian na zasobie, chcemy mieć gwarancję, że nasz system będzie docelowo spójny (*eventual consistency*).

Po zapisaniu zmian do bazy danych musimy mieć pewność, że uda się wyemitować event, na który nasłuchują inne usługi.  
Jeśli event nie zostanie wysłany, proces biznesowy nie zostanie w pełni zakończony, a system stanie się niespójny.

### Dlaczego to problem?

- Baza danych i message broker to dwa różne systemy – nie możemy w 100% zagwarantować, że obydwa zadziałają jednocześnie.
- Sieć i narzędzia mogą być chwilowo niedostępne.
- Jeśli transakcja w bazie danych została zatwierdzona, a wysyłka eventu się nie uda, to nie cofniemy zmian w bazie (jest już po zakończeniu transakcji).
- Jeśli wyślemy event **przed** zakończeniem transakcji, ryzykujemy, że event pójdzie w świat, mimo że zapis w bazie danych się nie powiódł.

## Rozwiązanie – Outbox Pattern

Wzorzec **Outbox Pattern** zapewnia synchronizację zmian w bazie danych i emisji eventów.

### Jak to działa?

1. W ramach **tej samej transakcji** zapisujemy:
    - zmieniony zasób,
    - informację o zdarzeniu do tabeli `outbox`.
2. Osobny proces (np. scheduler lub **Change Data Capture** z narzędziem takim jak [Debezium](https://debezium.io/)):
    - odczytuje wiadomości z tabeli `outbox`,
    - wysyła je do brokera wiadomości,
    - ponawia próbę w razie niepowodzenia, aż do skutku.
3. Po poprawnym wysłaniu eventu oznaczamy go jako dostarczony i po pewnym czasie usuwamy.

### Zalety

- Gwarancja, że zmiana w bazie i event są zsynchronizowane.
- Odporność na chwilowe problemy z dostępnością brokera lub sieci.
- Możliwość ponawiania wysyłki aż do skutku (zakładamy, że konsument jest **idempotentny**).

## Podsumowanie

**Outbox Pattern** Dzięki temu podejściu chwilowe problemy z wysyłką nie powodują utraty zdarzeń, a aplikacja pozostaje docelowo spójna.

Przykład implementacji mechanizmu outbox: [outbox](common/src/test/java/pl/kopytka/common/outbox)
Przykład użycia mechanizmu outbox: 
* Zapis do bazy z emitowaniem eventu: [CustomerOutboxWriter](customer-service/src/main/java/pl/kopytka/customer/messaging/CustomerOutboxWriter.java)
* Pobranie z bazy eventu i wysyłka: [CustomerOutboxPublisher](customer-service/src/main/java/pl/kopytka/customer/messaging/CustomerOutboxPublisher.java)


Warto podkreślić, że w projekcie należy zadbać o odpowiednie nazewnictwo.
Obecnie metoda `publish` zapisuje event, który ma zostać w przyszłości wyemitowany do bazy danych, a nie emituje go bezpośrednio.
Niektórzy preferują inne podejście do nazewnictwa i zmieniają nazwę np. z `publish` na `saveOutboxEvent`.
Tak samo nazwę interfejsu którego implementacja zapisuje do tabeli outbox, zamiast `..EventPublisher` używają np. `..EventOutbox`. 
Nazewnictwo zawsze należy dostosować do projektu :) 