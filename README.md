# Gateway i Service Discovery w architekturze rozproszonej

W architekturze rozproszonej, gdy skalujemy nasz system, musimy zachować **transparentność** dla klientów.  
Oznacza to, że żądania z zewnątrz powinny trafiać zawsze w jedno miejsce, niezależnie od tego, czy system składa się z jednej aplikacji, czy wielu mikroserwisów.

---

## Gateway Pattern

[Gateway](gateway)

W celu spełnienia tego wymagania zaimplementowaliśmy **Gateway Pattern** przy użyciu biblioteki **Spring Cloud Gateway**.  
Usługa gateway działa jako centralny punkt wejścia i przekierowuje ruch do odpowiednich mikroserwisów.

Jednak gateway **nie może zawierać statycznie zaszytych adresów** mikroserwisów, ponieważ mogą one dynamicznie się skalować (powstają nowe instancje).

---

## Service Discovery z Eureka

[Eureka Server](eureka)

Dlatego wdrożyliśmy mechanizm **Service Discovery** oparty na **Eureka Server**.  
Każdy mikroserwis został skonfigurowany jako **Eureka Client**, a Eureka Server rejestruje wszystkie dostępne instancje.

Dzięki temu gateway, współpracując z Eureka Serverem i korzystając z wbudowanego load balancera, może dynamicznie delegować ruch do dowolnej instancji mikroserwisu.

Każdy mikroserwis rejestruje się w Eureka Server z nazwą którą ma podaną w pliku `application.yml`
```
spring:
  application:
    name: <NAZWA_USŁUGI>
```

I to ta nazwa jest używana do komunikacji z mikroserwisem przez gateway: ([GatewayApplication](gateway/src/main/java/pl/kopytka/GatewayApplication.java)

---

## Przykłady

- Konsola Eureka:  
  [`http://localhost:8070`](http://localhost:8070) – zobacz, jakie usługi są dostępne i w ilu instancjach.

- Przykład wywołań usług:

    - Bezpośrednie wywołanie mikroserwisu (wewnętrzne):  
      `http://localhost:8581/api/customers`

    - Wywołanie przez gateway (zalecane z zewnątrz):  
      `http://localhost:9000/api/customers`

Z perspektywy klienta zewnętrznego jedynym znanym adresem jest adres gateway.

