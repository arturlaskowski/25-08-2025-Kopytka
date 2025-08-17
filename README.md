# Stawianie środowiska

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

---

### Sprawdzenie działania

1. Uruchom wszystkie mikroserwisy.

2. Wyślij żądanie:

   ```
   POST http://localhost:8581/api/customers
   ```

   Z body:

   ```json
   {
     "firstName": "Ferdynand",
     "lastName": "Kiepski",
     "email": "ferdynand.kiepski@example.com"
   }
   ```

3. Po udanym utworzeniu klienta sprawdź, czy:

  * jego ID zostało zreplikowane do schematu `order_schema` w tabeli `customer_view`,
  * w schemacie `payment_schema` w tabeli `wallets` utworzono dla niego portfel.

Jeśli chcesz podejrzeć wiadomości, możesz to zrobić w GUI do Kafki.
Jeśli dane zostały poprawnie zreplikowane to znaczy, że infrastruktura działa i można przejść dalej!

---

### Czyszczenie infrastruktury

Polecenie z pliku [docker-clean.sh](infrastructure/docker-clean.sh) usuwa całą infrastrukturę.
Zwalnia to zasoby i zapobiega kolizjom przy przełączaniu się na projekt **Punktozaur**.

Jeśli stworzyłeś lub zmodyfikowałeś schematy Avro dla wiadomości ([resources-avro](common/src/main/resources/avro)), to usuń katalog
[avro](common/src/main/java/pl/kopytka/avro) i ponownie skompiluj projekt common, np. używając polecenia `mvn compile`.



