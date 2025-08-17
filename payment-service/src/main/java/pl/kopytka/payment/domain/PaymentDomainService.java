package pl.kopytka.payment.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentDomainService {

    public void makePayment(Payment payment, Wallet wallet) {
        try {
            payment.initialize();
            payment.validatePaymentPrice();
            validateWalletAmount(payment, wallet);
            payment.complete();
            wallet.subtractCreditAmount(payment.getPrice());
            log.info("Payment completed, order id: {}", payment.getOrderId().id());
        } catch (PaymentDomainException e) {
            payment.rejected(e.getMessage());
            log.info("Payment rejected, order id: {}", payment.getOrderId().id());
        }
    }

    public void cancelPayment(Payment payment, Wallet wallet) {
        payment.validatePaymentPrice();
        payment.cancel();  // This will throw PaymentDomainException if already cancelled
        wallet.addCreditAmount(payment.getPrice());
        log.info("Payment cancelled, order id: {}", payment.getOrderId().id());
    }

    private void validateWalletAmount(Payment payment, Wallet wallet) {
        if (payment.getPrice().amount().compareTo(wallet.getAmount().amount()) > 0) {
            throw new PaymentDomainException("Payment price: " + payment.getPrice().amount() +
                    " must be less than or equal to wallet amount: " + wallet.getAmount().amount());
        }
    }
}
