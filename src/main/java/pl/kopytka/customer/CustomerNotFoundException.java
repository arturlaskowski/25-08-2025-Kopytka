package pl.kopytka.customer;

import java.util.UUID;

class CustomerNotFoundException extends RuntimeException {

    public static String createExceptionMessage(UUID id) {
        return String.format("Could not find customer with customerId: %s", id);
    }

    public CustomerNotFoundException(final UUID id) {
        super(createExceptionMessage(id));
    }
}