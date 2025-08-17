package pl.kopytka.payment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.Payment;
import pl.kopytka.payment.domain.PaymentDomainService;
import pl.kopytka.payment.domain.PaymentStatus;
import pl.kopytka.payment.domain.Wallet;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReprocessorService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final PaymentDomainService paymentDomainService;

    @Scheduled(fixedDelayString = "${payment.reprocess.interval-in-seconds:30}", timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void reprocessRejectedPayments() {
        log.info("Starting scheduled reprocessing of rejected payments at {}", Instant.now());

        List<Payment> rejectedPayments = paymentRepository.findByStatus(PaymentStatus.REJECTED);
        log.info("Found {} rejected payments to reprocess", rejectedPayments.size());

        int successCount = 0;
        int failedCount = 0;

        for (Payment payment : rejectedPayments) {
            try {
                log.info("Attempting to reprocess payment for order: {}", payment.getOrderId().id());

                Wallet wallet = walletRepository.findByCustomerId(payment.getCustomerId())
                        .orElseThrow(() -> new WalletNotFoundException(payment.getCustomerId()));

                // Reset status indirectly through the domain service
                payment.initialize();  // Will keep the existing ID but update the timestamp

                // Attempt to reprocess the payment
                paymentDomainService.makePayment(payment, wallet);

                // Save the updated payment and wallet state
                paymentRepository.save(payment);
                walletRepository.save(wallet);

                if (payment.isCompleted()) {
                    successCount++;
                    log.info("Successfully reprocessed payment for order: {}", payment.getOrderId().id());
                } else if (payment.isRejected()) {
                    failedCount++;
                    log.warn("Payment reprocessing still failed for order: {}", payment.getOrderId().id());
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Error reprocessing payment for order {}: {}", payment.getOrderId().id(), e.getMessage());
            }
        }

        log.info("Completed reprocessing rejected payments. Success: {}, Failed: {}", successCount, failedCount);
    }
}
