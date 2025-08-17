package pl.kopytka.customer.application.integration.payment;

import pl.kopytka.customer.domain.CustomerId;

public class PaymentServiceUnavailableException extends RuntimeException {

    public PaymentServiceUnavailableException(CustomerId customerId, Throwable cause) {
        super("Payment service is unavailable while creating wallet for customer: " + customerId.id(), cause);
    }
}
