package pl.kopytka.restaurant.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.restaurant.domain.entity.Restaurant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantEntityListener {

    private final RestaurantChangedEventPublisher restaurantChangedEventPublisher;

    @PostPersist
    public void afterCreate(Restaurant restaurant) {
        restaurantChangedEventPublisher.sendEvent(restaurant);
    }

    @PostUpdate
    public void afterUpdate(Restaurant restaurant) {
        restaurantChangedEventPublisher.sendEvent(restaurant);
    }
}