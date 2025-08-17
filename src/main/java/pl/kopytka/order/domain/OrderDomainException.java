package pl.kopytka.order.domain;

import pl.kopytka.common.domain.DomainException;

public class OrderDomainException extends DomainException {

    public OrderDomainException(String message) {
        super(message);
    }
}
