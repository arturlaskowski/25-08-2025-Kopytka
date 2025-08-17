# CQRS – jedna tabela, projekcja

Rozwiązanie znajdujące się na tym branchu nie jest zazwyczaj rozwiązaniem docelowym, lecz jedynie etapem przejściowym.
Polega ono na tym, że dwie encje korzystają z jednej tabeli 
* [Order](src/main/java/pl/kopytka/order/domain/Order.java) jedna służy do modyfikacji stanu, 
* [TrackingOrderProjection](src/main/java/pl/kopytka/trackorder/TrackingOrderProjection.java) a druga, często nazywana **projekcją** lub **repliką**, służy do odczytu danych dla danego scenariusza.

Dzięki takiemu podejściu możemy stworzyć cały moduł [trackorder](src/main/java/pl/kopytka/trackorder) i zweryfikować swój pomysł bez konieczności wprowadzania zmian w strukturze bazy danych ani ingerencji w istniejący kod aplikacji.

Jeśli to rozwiązanie okaże się spełniać nasze kryteria, kolejnym krokiem będzie stworzenie osobnej tabeli do trackowania zamówienia oraz implementacja replikacji danych. To zostanie zrealizowane w kolejnym branchu.
