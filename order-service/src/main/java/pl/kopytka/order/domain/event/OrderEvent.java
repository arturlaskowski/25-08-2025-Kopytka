package pl.kopytka.order.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kopytka.common.domain.event.DomainEvent;
import pl.kopytka.order.domain.Order;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public abstract class OrderEvent implements DomainEvent {
    private final Order order;
    private final Instant createdAt;

    OrderEvent(Order order) {
        this.order = order;
        this.createdAt = Instant.now();
    }
}
