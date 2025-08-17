package pl.kopytka.order.application.integration.customer;

import pl.kopytka.common.domain.valueobject.CustomerId;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(CustomerId customerId) {
        super("Customer not found with id: " + customerId.id());
    }
}
