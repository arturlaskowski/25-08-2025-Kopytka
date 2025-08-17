# CQRS – replikacja danych

**CQRS** (Command Query Responsibility Segregation) to podejście polegające na oddzieleniu modyfikacji stanu od jego odczytu.  
Można je zaimplementować nawet na jednej tabeli, ale znacznie więcej korzyści przynosi, gdy modyfikacja i odczyt są realizowane na osobnych tabelach lub nawet w osobnych bazach danych.

Dzięki temu możemy dostosować strukturę danych pod potrzeby odczytu tak, aby była jak najbardziej wydajna –  
np. przechowywać tylko te dane, które są potrzebne do wyświetlania na ekranie, w jednej tabeli, nawet jeśli pochodzą one z wielu innych tabel.  
Możemy także tworzyć inne indeksy niż w tabelach do modyfikacji stanu, a nawet używać innych technologii bazodanowych, np. **ElasticSearch**, który wspiera wyszukiwanie pełnotekstowe.

Na tym branchu znajduje się przykład replikowania danych do innego modułu w celu ich efektywnego odczytu.  
Moduł `trackorder` to nowy moduł, który przechowuje tylko część informacji o zamówieniu – [TrackingOrderProjection](src/main/java/pl/kopytka/trackorder/TrackingOrderProjection.java).  
Ta tabela mogłaby być rozszerzona o inne dane potrzebne do wyświetlania, np. pochodzące z modułu `delivery`.

Podczas tworzenia klienta emitowany jest event `CustomerCreatedEvent`, który jest publikowany przez moduł `customer`.  
Moduł `order` nasłuchuje na niego i replikuje potrzebne informacje o kliencie do swojej tabeli – [CustomerViewService](src/main/java/pl/kopytka/order/replication/CustomerViewService.java).  
Dzięki temu moduł `order` staje się bardziej autonomiczny – przy tworzeniu zamówienia nie musi już pytać modułu `customer`, czy klient istnieje, tylko może to sprawdzić w ramach własnego modułu [CreateOrderHandler](src/main/java/pl/kopytka/order/command/create/CreateOrderHandler.java).

Do replikacji danych został wykorzystany mechanizm `ApplicationEventPublisher` ze Springa.  
Należy jednak pamiętać, że jego domyślna implementacja działa synchronicznie,  
co oznacza, że czas modyfikacji stanu zostaje wydłużony o czas replikacji danych.

W takich przypadkach często przechodzi się na komunikację asynchroniczną.  
Najlepiej wówczas wykorzystać message broker (np. **Kafka**, **RabbitMQ**) – ale to już temat na kolejny branch 😄
