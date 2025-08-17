package pl.kopytka.order.application.command;

import java.util.UUID;

public class InvalidOrderException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Could not find customer with customerId: %s";

    public static String createExceptionMessage(UUID id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

    public InvalidOrderException(final UUID id) {
        super(createExceptionMessage(id));
    }
}