package pl.kopytka.order.domain.event;


import pl.kopytka.order.domain.Order;

public class OrderCreatedEvent extends OrderEvent {

    public OrderCreatedEvent(Order order) {
        super(order);
    }
}