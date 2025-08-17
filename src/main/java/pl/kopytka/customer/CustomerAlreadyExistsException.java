package pl.kopytka.customer;

public class CustomerAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Customer with email: %s already exists";

    public static String createExceptionMessage(String email) {
        return String.format(MESSAGE_TEMPLATE, email);
    }

    public CustomerAlreadyExistsException(String email) {
        super(createExceptionMessage(email));
    }
}