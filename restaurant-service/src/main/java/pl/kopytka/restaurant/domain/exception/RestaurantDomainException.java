package pl.kopytka.restaurant.domain.exception;

import pl.kopytka.common.domain.exception.DomainException;

public class RestaurantDomainException extends DomainException {
    
    public RestaurantDomainException(String message) {
        super(message);
    }
    
    public RestaurantDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
