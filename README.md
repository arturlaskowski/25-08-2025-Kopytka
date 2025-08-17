# CQRS - prosty

Sam koncept **CQRS** (Command Query Responsibility Segregation) jest stosunkowo prosty, polega na oddzieleniu operacji modyfikujących stan (logika biznesowa) od operacji odczytu.

---

## Implementacja
Ten branch zawiera bardzo prostą implementację CQRS dla modułu `order`.

- Pakiet [query](src/main/java/pl/kopytka/order/application/query) zawiera operacje **odczytu**.
- Pakiet [command](src/main/java/pl/kopytka/order/application/command) zawiera operacje **modyfikacji stanu**.

Oddzielenie odbywa się na poziomie warstwy serwisów i repozytoriów.  
Dzięki temu można zastosować różne strategie testowania, dobrać młotek do problemu :D 
