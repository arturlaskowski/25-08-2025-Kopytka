package pl.kopytka.order.domain.event;

import pl.kopytka.order.domain.Order;

public class OrderApprovedEvent extends OrderEvent {
    public OrderApprovedEvent(Order order) {
        super(order);
    }
}
