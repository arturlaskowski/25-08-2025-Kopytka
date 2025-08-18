package pl.kopytka.payment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.payment.application.dto.CancelPaymentCommand;
import pl.kopytka.payment.application.dto.MakePaymentCommand;
import pl.kopytka.payment.application.exception.PaymentNotFoundException;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.Payment;
import pl.kopytka.payment.domain.PaymentDomainException;
import pl.kopytka.payment.domain.PaymentDomainService;
import pl.kopytka.payment.domain.Wallet;
import pl.kopytka.payment.domain.event.PaymentEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final PaymentDomainService paymentDomainService;
    private final DomainEventPublisher<PaymentEvent> eventPublisher;

    @Transactional
    public void makePayment(MakePaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        // Check if payment already exists for this order
        OrderId orderId = new OrderId(command.orderId());
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new PaymentDomainException("Payment already exists");
        }
        var customerId = new CustomerId(command.customerId());
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new WalletNotFoundException(customerId));

        Payment payment = new Payment(
                orderId,
                customerId,
                new Money(command.price())
        );

        var paymentEvent = paymentDomainService.makePayment(payment, wallet);
        paymentRepository.save(payment);
        eventPublisher.publish(paymentEvent);
    }

    @Transactional
    public void cancelPayment(CancelPaymentCommand command) {
        log.info("Processing payment cancellation for payment: {}", command.orderId());

        Payment payment = paymentRepository.findByOrderId(new OrderId(command.orderId()))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + command.orderId()));

        // Check if customer IDs match
        CustomerId requestCustomerId = new CustomerId(command.customerId());
        if (!payment.getCustomerId().equals(requestCustomerId)) {
            throw new PaymentDomainException("Customer ID does not match the payment");
        }

        Wallet wallet = walletRepository.findByCustomerId(requestCustomerId)
                .orElseThrow(() -> new WalletNotFoundException(requestCustomerId));

        var paymentEvent = paymentDomainService.cancelPayment(payment, wallet);

        paymentRepository.save(payment);
        walletRepository.save(wallet);
        eventPublisher.publish(paymentEvent);
    }
}
