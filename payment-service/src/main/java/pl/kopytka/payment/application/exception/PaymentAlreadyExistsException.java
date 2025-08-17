package pl.kopytka.payment.application.exception;


import pl.kopytka.common.domain.valueobject.OrderId;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(OrderId orderId) {
        super("Payment already exists for order: " + orderId.id());
    }
}
