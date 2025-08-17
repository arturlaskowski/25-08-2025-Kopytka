package pl.kopytka.customer.domain.event;

import pl.kopytka.customer.domain.Customer;

public class CustomerCreatedEvent extends CustomerEvent {

    public CustomerCreatedEvent(Customer customer) {
        super(customer);
    }
}