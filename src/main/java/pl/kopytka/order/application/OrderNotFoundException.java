package pl.kopytka.order.application;

import pl.kopytka.common.domain.OrderId;

public class OrderNotFoundException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Could not find order with orderId:  %s";

    public static String createExceptionMessage(OrderId orderId) {
        return String.format(MESSAGE_TEMPLATE, orderId.id());
    }

    public OrderNotFoundException(OrderId orderId) {
        super(createExceptionMessage(orderId));
    }
}