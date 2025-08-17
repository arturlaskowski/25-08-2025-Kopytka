package pl.kopytka.order.domain.event;

import pl.kopytka.order.domain.Order;

public class OrderPaidEvent extends OrderEvent {
    public OrderPaidEvent(Order order) {
        super(order);
    }
}
