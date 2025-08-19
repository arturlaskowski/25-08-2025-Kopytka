package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.*;
import pl.kopytka.common.outbox.OutboxService;
import pl.kopytka.payment.domain.Payment;
import pl.kopytka.payment.domain.event.PaymentCanceledEvent;
import pl.kopytka.payment.domain.event.PaymentCompletedEvent;
import pl.kopytka.payment.domain.event.PaymentEvent;
import pl.kopytka.payment.domain.event.PaymentRejectedEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class PaymentOutboxWriter {

    static final String MESSAGE_TYPE_PAYMENT_COMPLETED = "paymentCompleted";
    static final String MESSAGE_TYPE_PAYMENT_FAILED = "paymentFailed";
    static final String MESSAGE_TYPE_PAYMENT_CANCELED = "paymentCanceled";

    private final OutboxService outboxService;

    protected void on(PaymentEvent event) {
        switch (event) {
            case PaymentCompletedEvent completedEvent -> handlePaymentCompleted(completedEvent);
            case PaymentRejectedEvent rejectedEvent -> handlePaymentRejected(rejectedEvent);
            case PaymentCanceledEvent canceledEvent -> handlePaymentCanceled(canceledEvent);
            default -> log.error("Unsupported payment event type: {}", event.getClass().getName());
        }
    }

    private void handlePaymentCompleted(PaymentCompletedEvent event) {
        Payment payment = event.getPayment();

        PaymentCompletedAvroEvent completedEvent = PaymentCompletedAvroEvent.newBuilder()
                .setPaymentId(UUID.fromString(payment.getId().id().toString()))
                .setCustomerId(UUID.fromString(payment.getCustomerId().id().toString()))
                .setOrderId(UUID.fromString(payment.getOrderId().id().toString()))
                .setPrice(payment.getPrice().amount())
                .setCreatedAt(event.getCreatedAt())
                .build();

        PaymentEventAvroModel eventMessage = PaymentEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(PaymentEventType.PAYMENT_COMPLETED)
                .setPayload(completedEvent)
                .build();

        outboxService.save(MESSAGE_TYPE_PAYMENT_COMPLETED, payment.getOrderId().id().toString(), eventMessage);
        log.info("Outbox entry saved for payment completed - order: {}", payment.getOrderId().id());
    }

    private void handlePaymentRejected(PaymentRejectedEvent event) {
        Payment payment = event.getPayment();

        PaymentFailedAvroEvent failedEvent = PaymentFailedAvroEvent.newBuilder()
                .setPaymentId(UUID.fromString(payment.getId().id().toString()))
                .setCustomerId(UUID.fromString(payment.getCustomerId().id().toString()))
                .setOrderId(UUID.fromString(payment.getOrderId().id().toString()))
                .setCreatedAt(event.getCreatedAt())
                .setFailureMessages(payment.getErrorMessage() != null ? payment.getErrorMessage() : "Payment processing failed")
                .build();

        PaymentEventAvroModel eventMessage = PaymentEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(PaymentEventType.PAYMENT_FAILED)
                .setPayload(failedEvent)
                .build();

        outboxService.save(MESSAGE_TYPE_PAYMENT_FAILED, payment.getOrderId().id().toString(), eventMessage);
        log.info("Outbox entry saved for payment failed - order: {}", payment.getOrderId().id());
    }

    private void handlePaymentCanceled(PaymentCanceledEvent event) {
        Payment payment = event.getPayment();

        PaymentCancelledAvroEvent canceledEvent = PaymentCancelledAvroEvent.newBuilder()
                .setPaymentId(UUID.fromString(payment.getId().id().toString()))
                .setCustomerId(UUID.fromString(payment.getCustomerId().id().toString()))
                .setOrderId(UUID.fromString(payment.getOrderId().id().toString()))
                .setCreatedAt(event.getCreatedAt())
                .build();

        PaymentEventAvroModel eventMessage = PaymentEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(PaymentEventType.PAYMENT_CANCELLED)
                .setPayload(canceledEvent)
                .build();

        outboxService.save(MESSAGE_TYPE_PAYMENT_CANCELED, payment.getOrderId().id().toString(), eventMessage);
        log.info("Outbox entry saved for payment canceled - order: {}", payment.getOrderId().id());
    }
}
