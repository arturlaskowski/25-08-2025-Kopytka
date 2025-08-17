package pl.kopytka.common.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record RestaurantId(UUID restaurantId) {

    public static RestaurantId newOne() {
        return new RestaurantId(UUID.randomUUID());
    }

    public UUID id() {
        return restaurantId;
    }
}
