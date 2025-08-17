# Testy kontraktowe

## Wprowadzenie

Testy kontraktowe pozwalają zweryfikować, czy usługi się dogadają po wdrożeniu.
Z reguły wplatane są w proces CI/CD, ale lokalnie jesteśmy w stanie zweryfikować działanie tego mechanizmu. 
Kontrakty zostały zaimplementowane za pomocą biblioteki Spring Cloud Contract.

## Przykład 

Za przykład nam będzie służył kontrakt między `customer-service` a `payment-service`:
- **customer-service** komunikuje się z **payment-service**, żeby utworzyć portfel
- **payment-service** - provider (dostarcza API)
- **customer-service** - consumer (konsumuje API)

## Pisanie kontraktów

### Konfiguracja po stronie providera

W `payment-service` w pliku [pom](payment-service/pom.xml) deklarujemy, że na potrzeby kontraktów będziemy używać klasy  [ContractTestBase.java](payment-service/src/test/java/pl/kopytka/payment/contracts/ContractTestBase.java)
, która imituje zachowanie aplikacji po to, żeby sprawdzić, czy kontrakty dla API są prawidłowe.

### Definiowanie kontraktów po stronie providera

Kontrakty definiujemy w formacie Groovy. Przykład kontraktu do tworzenia portfela:
📄 [create_wallet.groovy](payment-service/src/test/resources/contracts/customer-service/create_wallet.groovy)


Po zdefiniowaniu kontraktu uruchamiamy dla providera `mvn install`.

## Testowanie po stronie providera

Na podstawie kontraktów podczas kompilacji automatycznie generowane są:
1. **Testy jednostkowe dla providera** - można podejrzeć w katalogu `target` w `payment-service`
2. Jeśli testy przejdą, znaczy to, że provider jest zgodny z kontraktem
3. Provider automatycznie generuje **stubby (.jar)** które będą używane przez konsumenta API - można podejrzeć w katalogu `target` w `payment-service`

## Testowanie po stronie consumera
Kolejność lokalnie ma znaczenie, ponieważ najpierw musimy zbudować providera, żeby wygenerował się stub dla consumera. W procesie CI/CD kolejność nie ma już znaczenia,
ponieważ mikroserwisy są wersjonowane i consumer pobierze odpowiednią wersję stub JAR, np. z Nexusa.
Consumer używa wygenerowanych stubbów do testowania swojej logiki bez potrzeby uruchamiania prawdziwego serwisu.
Trzeba jawnie zadeklarować w kodzie, że korzystamy ze stuba innego mikroserwisu.

**Przykład implementacji**: [PaymentServiceContractTest](customer-service/src/test/java/pl/kopytka/customer/contracts/PaymentServiceContractTest.java)

## Korzyści

- **Izolacja testów** - testowanie bez potrzeby uruchamiania wszystkich serwisów
- **Weryfikacja zgodności** - automatyczne sprawdzenie czy provider i consumer są ze sobą kompatybilne
- **Integracja z CI/CD** - możliwość automatycznego uruchamiania w pipeline'ie