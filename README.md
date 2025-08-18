# State Transfer Event - A duplikacja wiadomości na message brocker

W przypadku podejścia **state transfer event**, gdzie replikujemy cały stan na potrzeby optymalizacji odczytu, nie musimy używać wzorca *Inbox Pattern*.
Możemy zamiast tego polegać na wersjonowaniu zasobu (JPA).

Taki warunek po stronie konsumenta **załatwia problem duplikacji wiadomości**:
```java
if (event.version > currentResource.version) {
    apply(event);
} else {
    ignore(event); // duplikat albo stary event
}
```

Na tym branch za przykład może posłużyć replikowanie informacji o produkcie z serwisu `restaurant-service` do nowego mikroserwisu `product-search-service`,
który powstał na potrzeby optymalizacji odczytu.

Przykład implementacji konsumenta takich eventów znajdziesz tutaj:
[RestaurantChangedStateEventListener](product-search-service/src/main/java/pl/kopytka/customer/RestaurantChangedStateEventListener.java)

Oczywiście po stronie odczytu struktura danych w bazie może się różnić od tej po stronie modyfikacji stanu (tak jak tu).
Co więcej, możemy użyć innego rodzaju bazy danych.

Emitowanie **state transfer event** jest najczęściej realizowane automatycznie po każdej zmianie stanu – nie emitujemy eventu jawnie w kodzie, 
lecz korzystamy z podejścia, które nasłuchuje na zmiany i samo emituje event. 
W Spring Data możemy skorzystać z adnotacji `@EntityListener`, np.:
[RestaurantEntityListener](restaurant-service/src/main/java/pl/kopytka/restaurant/domain/RestaurantEntityListener.java)

Warto zwrócić uwagę, że dzięki temu podejściu eventy są emitowane **dopiero po commicie do bazy danych**,
poprzez adnotacje `@PostPersist` i `@PostUpdate`.
To daje nam gwarancję, że zmiany w bazie danych zostały już wykonane. 
W naszych innych emitowanych eventach **takiej gwarancji nie mamy**! (coś powinniśmy z tym zrobić :)

