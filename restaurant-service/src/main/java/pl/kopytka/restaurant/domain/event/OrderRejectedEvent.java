package pl.kopytka.restaurant.domain.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderRejectedEvent extends RestaurantOrderEvent {
    private final String rejectionReason;

    public OrderRejectedEvent(UUID restaurantId, UUID orderId, String rejectionReason) {
        super(restaurantId, orderId);
        this.rejectionReason = rejectionReason;
    }
}
