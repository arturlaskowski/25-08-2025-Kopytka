package pl.kopytka.order.application.integration.payment;

import pl.kopytka.order.domain.CustomerId;
import pl.kopytka.order.domain.Money;
import pl.kopytka.order.domain.OrderId;

public class PaymentServiceUnavailableException extends RuntimeException {

    public PaymentServiceUnavailableException(OrderId orderId, CustomerId customerId, Money amount, Throwable cause) {
        super("Payment service is unavailable for order: " + orderId.id() + 
              ", customer: " + customerId.id() + 
              ", amount: " + amount.amount(), cause);
    }
}
