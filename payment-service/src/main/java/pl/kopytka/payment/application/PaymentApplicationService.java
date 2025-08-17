package pl.kopytka.payment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.payment.application.dto.CancelPaymentCommand;
import pl.kopytka.payment.application.dto.MakePaymentCommand;
import pl.kopytka.payment.application.dto.PaymentResult;
import pl.kopytka.payment.application.exception.PaymentNotFoundException;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final PaymentDomainService paymentDomainService;

    @Transactional
    public PaymentResult makePayment(MakePaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        // Check if payment already exists for this order
        OrderId orderId = new OrderId(command.orderId());
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new PaymentDomainException("Payment already exists");
        }

        Wallet wallet = walletRepository.findByCustomerId(new CustomerId(command.customerId()))
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for customer: " + command.customerId()));

        Payment payment = new Payment(
                orderId,
                new CustomerId(command.customerId()),
                new Money(command.price())
        );

        paymentDomainService.makePayment(payment, wallet);
        paymentRepository.save(payment);
        return new PaymentResult(payment.getId().paymentId(), payment.isCompleted(), payment.getErrorMessage());

    }

    @Transactional
    public void cancelPayment(CancelPaymentCommand command) {
        log.info("Processing payment cancellation for payment: {}", command.orderId());

        // We are using orderId as the payment ID in the test
        Payment payment = paymentRepository.findByOrderId(new OrderId(command.orderId()))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + command.orderId()));

        // Check if customer IDs match
        CustomerId requestCustomerId = new CustomerId(command.customerId());
        if (!payment.getCustomerId().equals(requestCustomerId)) {
            throw new PaymentDomainException("Customer ID does not match the payment");
        }

        Wallet wallet = walletRepository.findByCustomerId(requestCustomerId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for customer: " + command.customerId()));

        paymentDomainService.cancelPayment(payment, wallet);

        // Save the updated payment and wallet
        paymentRepository.save(payment);
        walletRepository.save(wallet);
    }
}
