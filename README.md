# CQRS Command Handler

Modyfikacja stanu często bywa skomplikowana, a jak wiadomo w IT, jeśli coś jest skomplikowane, to lepiej rozbić problem na mniejsze części i rozwijać je niezależnie. 
W tym celu często wykorzystywane jest podejście **Command Handler** (implementacja wzorca Mediator).

- **Command** – intencja zmiany stanu (DTO zawierające wszystkie dane potrzebne do zmiany oraz nazwę opisującą tę zmianę).
- **Command Handler** – klasa obsługująca dokładnie jedną komendę.

W Springu często implementujemy to podejście wykorzystując mechanizmy frameworka, czyli podczas startu kontekstu wszystkie beany będące command handlerami rejestrują się, tworząc mapę,
w której jest jasna informacja, jaka komenda jest obsługiwana przez jaki handler (Mediator). Przykład znajduje się w: [Implementacja mediatora](src/main/java/pl/kopytka/common/command/SynchronousCommandHandlerExecutor.java)

Dzięki temu kontroler może przekazać tylko komendę do mediatora, a mediator sam dobierze odpowiedni handler. 
Przykład kontrolera: [OrderController](src/main/java/pl/kopytka/order/web/OrderController.java)

Obsługa komend dla modułu order znajduje się w: [wykorzystanie mechanizmu command-handler](src/main/java/pl/kopytka/order/command)

Dzięki temu podejściu mediator może mieć wiele implementacji, np.:
- Dodawanie dodatkowego logowania w celu weryfikacji czasu trwania danego procesu (np. w osobnym profilu Springa).
- Zmiana na podejście asynchroniczne, jeśli zajdzie taka potrzeba.
