package pl.kopytka.restaurant.domain;

import pl.kopytka.restaurant.domain.entity.Restaurant;

public interface RestaurantChangedEventPublisher {
    void sendEvent(Restaurant restaurant);
}
