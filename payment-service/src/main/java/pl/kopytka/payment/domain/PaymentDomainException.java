package pl.kopytka.payment.domain;


public class PaymentDomainException extends DomainException {
    
    public PaymentDomainException(String message) {
        super(message);
    }
    
    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
