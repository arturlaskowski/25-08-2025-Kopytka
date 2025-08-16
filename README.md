# Szkolenie: Wzorce projektowe w mikroserwisach

Cześć!  
Dziękujemy, że jesteś na szkoleniu dotyczącym wzorców projektowych w mikroserwisach.

To repozytorium **`kopytka`** ma na celu pokazywać przykładowe implementacje wzorców.  
Zadania, które pomogą lepiej utrwalić zdobytą wiedzę, będą realizowane w osobnym projekcie **`punktozaur`**.

W tym repozytorium każdy branch reprezentuje inne omawiane rozwiązanie.  
W pliku README na danym branchu zawsze znajdziesz opis zastosowanego rozwiązania.

---

## Branch początkowy

Jest to punkt startowy naszej podróży.  
Mamy system monolityczny, w którym chcemy przejść do architektury rozproszonej.  
Na początek skupimy się na modularyzacji systemu.

---

## Testowanie aplikacji

Funkcjonalności aplikacji można testować za pomocą testów automatycznych.  
W tym projekcie testy akceptacyjne to testy po **REST API**, które sprawdzają całe flow.

Jeśli wolisz testować manualnie, do każdego brancha dostarczany jest plik, który możesz zaimportować do Postmana: [Kolekcja Postman](kopytka_now_1.postman_collection.json)

---

## Rozdzielenie modułów

Obecną implementację chcemy rozdzielić na dwa autonomiczne moduły: **order** i **customer**.  
Aby to zrobić, musimy pozbyć się istniejących powiązań w bazie danych opartych na kluczach obcych.

Przed wykonaniem takiego kroku, jak rozbijanie aplikacji na moduły, dobrze jest napisać testy na najwyższym poziomie,  
które sprawdzają obserwowalne zachowanie aplikacji — ponieważ ono nie powinno się zmienić.

Po refaktoryzacji testy te weryfikują, czy nie zostały naruszone wcześniejsze założenia.  
Takie testy jak i rozbicie na moduły zostało zaimplementowane na następnym branchu **`modularity`**.

