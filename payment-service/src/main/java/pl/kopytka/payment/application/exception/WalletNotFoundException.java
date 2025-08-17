package pl.kopytka.payment.application.exception;

import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.payment.domain.WalletId;

public class WalletNotFoundException extends RuntimeException {
    
    public WalletNotFoundException(WalletId walletId) {
        super("Wallet with ID " + walletId.id() + " not found");
    }

    public WalletNotFoundException(CustomerId customerId) {
        super("Wallet for customer ID " + customerId.id() + " not found");
    }
}
