package pl.kopytka.order.domain.event;

import pl.kopytka.order.domain.Order;

public class OrderCancelInitiatedEvent extends OrderEvent {

    public OrderCancelInitiatedEvent(Order order) {
        super(order);
    }
}