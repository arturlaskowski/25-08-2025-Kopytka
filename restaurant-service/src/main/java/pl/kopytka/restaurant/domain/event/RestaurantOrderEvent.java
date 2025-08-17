package pl.kopytka.restaurant.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kopytka.common.domain.event.DomainEvent;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public abstract class RestaurantOrderEvent implements DomainEvent {
    private final UUID restaurantId;
    private final UUID orderId;
}
