package pl.kopytka.order.application.integration.payment;

import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;

public class PaymentProcessingFailedException extends RuntimeException {

    public PaymentProcessingFailedException(OrderId orderId, CustomerId customerId, Money amount, String reason) {
        super("Payment processing failed for order: " + orderId.id() + 
              ", customer: " + customerId.id() + 
              ", amount: " + amount.amount() + 
              ". Reason: " + reason);
    }
}
