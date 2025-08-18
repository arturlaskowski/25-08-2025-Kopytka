package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kopytka.avro.payment.*;
import pl.kopytka.payment.domain.Payment;
import pl.kopytka.payment.domain.event.PaymentCanceledEvent;
import pl.kopytka.payment.domain.event.PaymentCompletedEvent;
import pl.kopytka.payment.domain.event.PaymentEvent;
import pl.kopytka.payment.domain.event.PaymentRejectedEvent;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class KafkaPaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEventAvroModel> kafkaTemplate;
    private final TopicsConfigData topicsConfigData;

    @TransactionalEventListener
    @Async
    public void handleEventAfterCommitAndSendToKafka(PaymentEvent event) {
        switch (event) {
            case PaymentCompletedEvent completedEvent -> publishPaymentCompletedEvent(completedEvent);
            case PaymentRejectedEvent rejectedEvent -> publishPaymentFailedEvent(rejectedEvent);
            case PaymentCanceledEvent canceledEvent -> publishPaymentCanceledEvent(canceledEvent);
            default -> log.error("Unsupported payment event type: {}", event.getClass().getName());
        }
    }

    private void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
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

        kafkaTemplate.send(topicsConfigData.getPaymentEvent(), payment.getOrderId().id().toString(), eventMessage);
        log.info("Payment completed event sent to kafka for order id: {}", payment.getOrderId().id());
    }

    private void publishPaymentFailedEvent(PaymentRejectedEvent event) {
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

        kafkaTemplate.send(topicsConfigData.getPaymentEvent(), payment.getOrderId().id().toString(), eventMessage);
        log.info("Payment failed event sent to kafka for order id: {}", payment.getOrderId().id());
    }

    private void publishPaymentCanceledEvent(PaymentCanceledEvent event) {
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

        kafkaTemplate.send(topicsConfigData.getPaymentEvent(), payment.getOrderId().id().toString(), eventMessage);
        log.info("Payment canceled event sent to kafka for order id: {}", payment.getOrderId().id());
    }
}
