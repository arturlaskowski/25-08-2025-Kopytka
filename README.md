# Dynamiczna komunikacja między mikroserwisami z Feign Client

Dotychczas mikroserwisy komunikowały się ze sobą, korzystając ze statycznie zdefiniowanych adresów w plikach `application.yml`.
Takie rozwiązanie jednak nie pozwala na skalowanie systemu ani dynamiczne zwiększanie liczby instancji mikroserwisów.

Skoro mamy już wdrożony **Eureka Server** (Discovery Service), który zna wszystkie dostępne instancje mikroserwisów, to możemy wykorzystać te informacje do bardziej elastycznej komunikacji.

## Feign Client + Eureka

W tym celu użyliśmy **Feign Clienta** z biblioteki **Spring Cloud**. Feign integruje się z Eureką i umożliwia dynamiczne przekierowywanie żądań przy użyciu wbudowanego load balancera.
Po naszej stronie wystarczy zadeklarować, do jakiego mikroserwisu chcemy wysłać zapytanie. Nazwa musi odpowiadać wartości konfiguracyjnej tego mikroserwisu:

```yaml
spring:
  application:
    name: <NAZWA_USŁUGI>
```

To właśnie ta nazwa jest rejestrowana w Eurece. Teraz wystarczy wskazać tylko nazwę usługi, a Feign i load balancer zadbają o to, by ruch został przekierowany do odpowiedniej instancji.

## Przykład użycia

Przykładowa implementacja Feign Clienta w module `customer-service`, komunikująca się z `payment-service`:  
👉 [PaymentServiceFeignClient.java](customer-service/src/main/java/pl/kopytka/customer/application/integration/payment/PaymentServiceFeignClient.java)
Implementacja tego interfejsu jest już robiona automatycznie przez bibliotekę Feign.