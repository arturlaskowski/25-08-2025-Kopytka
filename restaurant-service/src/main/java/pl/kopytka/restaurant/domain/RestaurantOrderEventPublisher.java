package pl.kopytka.restaurant.domain;

import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.restaurant.domain.event.RestaurantOrderEvent;

public interface RestaurantOrderEventPublisher extends DomainEventPublisher<RestaurantOrderEvent> {

    void publish(RestaurantOrderEvent event);
}
