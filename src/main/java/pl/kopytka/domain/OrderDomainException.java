package pl.kopytka.domain;

public class OrderDomainException extends RuntimeException {

    public OrderDomainException(String message) {
        super(message);
    }
}
