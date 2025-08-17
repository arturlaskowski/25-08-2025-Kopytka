# Obsługa cyklicznych zadań w środowisku rozproszonym

Podczas skalowania aplikacji i korzystania z mechanizmów wykonywania cyklicznych operacji, musimy zadbać o to, aby procesy, które mają wykonać się tylko raz, faktycznie wykonały się tylko raz.

**Przykład:**  
Jeśli użyjemy adnotacji `@Scheduled` i ustawimy generowanie raportu co godzinę, a nasz mikroserwis działa w pięciu instancjach, to raport wygeneruje się pięć razy — po jednym razie na każdej instancji.
Może to powodować nadmiarowe operacje i problemy, zwłaszcza gdy zadanie modyfikuje stan (np. przetwarza płatności).

Aby uniknąć takich sytuacji, stosujemy **distributed lock** — czyli mechanizm, który zapewnia, że konkretne zadanie cykliczne zostanie wykonane tylko raz w skali całego 'klastra', niezależnie od liczby instancji mikroserwisu.

---

## ShedLock

W tym celu do systemu została dodana biblioteka **ShedLock**, która umożliwia realizację blokad rozproszonych.

Konfiguracja jest parametryzowana — mikroserwis, który potrzebuje obsługi cyklicznych zadań, może włączyć tę funkcję za pomocą właściwości:
`kopytka.scheduling.enabled=true`[Klasa konfiguracyjna](common/src/main/java/pl/kopytka/common/config/SchedulingConfig.java)
To podejście pozwala na łatwe włączenie lub wyłączenie obsługi cyklicznych zadań np. na potrzeby testów innych komponentów.

ShedLock potrzebuje tabeli w bazie danych, która pozwala mu sprawdzać, czy dane zadanie cykliczne jest aktualnie wykonywane, czy można je zablokować i uruchomić.  
Schemat tabeli znajdziesz tutaj:  
👉 [Schemat tabeli](payment-service/src/main/resources/schema.sql)

Aby użyć blokowania z wykorzystaniem ShedLock, należy oznaczyć cykliczną metodę adnotacją `@SchedulerLock`.  
Przykład implementacji znajduje się w klasie:  
👉 [PaymentReprocessorService](payment-service/src/main/java/pl/kopytka/payment/application/PaymentReprocessorService.java)
