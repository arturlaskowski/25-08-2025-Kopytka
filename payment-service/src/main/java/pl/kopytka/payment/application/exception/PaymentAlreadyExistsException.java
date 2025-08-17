package pl.kopytka.payment.application.exception;

import pl.kopytka.payment.domain.OrderId;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(OrderId orderId) {
        super("Payment already exists for order: " + orderId.id());
    }
}
