package pl.kopytka.order.application.exception;

import pl.kopytka.common.domain.valueobject.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("Order not found with id: " + orderId.id());
    }
}
