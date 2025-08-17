package pl.kopytka.payment.application.exception;

import pl.kopytka.common.domain.valueobject.CustomerId;

public class WalletAlreadyExistsException extends RuntimeException {

    public WalletAlreadyExistsException(CustomerId customerId) {
        super("Wallet already exists for customer: " + customerId.id());
    }
}
