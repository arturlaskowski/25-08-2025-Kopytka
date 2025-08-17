package pl.kopytka.order.application.integration.payment;

import pl.kopytka.order.domain.CustomerId;
import pl.kopytka.order.domain.Money;
import pl.kopytka.order.domain.OrderId;

public class PaymentProcessingFailedException extends RuntimeException {

    public PaymentProcessingFailedException(OrderId orderId, CustomerId customerId, Money amount, String reason) {
        super("Payment processing failed for order: " + orderId.id() + 
              ", customer: " + customerId.id() + 
              ", amount: " + amount.amount() + 
              ". Reason: " + reason);
    }
}
