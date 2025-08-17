package pl.kopytka.restaurant.domain.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderApprovedEvent extends RestaurantOrderEvent {
    public OrderApprovedEvent(UUID restaurantId, UUID orderId) {
        super(restaurantId, orderId);
    }
}
