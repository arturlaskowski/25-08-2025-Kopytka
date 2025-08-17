package pl.kopytka.order.domain.event;


import pl.kopytka.order.domain.Order;

public class OrderCanceledEvent extends OrderEvent {

    public OrderCanceledEvent(Order order) {
        super(order);
    }
}