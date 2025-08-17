package pl.kopytka.order.domain;


import pl.kopytka.common.domain.exception.DomainException;

public class OrderDomainException extends DomainException {
    
    public OrderDomainException(String message) {
        super(message);
    }
    
    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
