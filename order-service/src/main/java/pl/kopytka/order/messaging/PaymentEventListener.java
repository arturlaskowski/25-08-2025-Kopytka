package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.PaymentCancelledAvroEvent;
import pl.kopytka.avro.payment.PaymentCompletedAvroEvent;
import pl.kopytka.avro.payment.PaymentEventAvroModel;
import pl.kopytka.avro.payment.PaymentFailedAvroEvent;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.kafka.consumer.IdempotentKafkaConsumer;
import pl.kopytka.order.application.OrderApplicationService;

import java.util.List;

@Component
@RequiredArgsConstructor
class PaymentEventListener extends IdempotentKafkaConsumer<PaymentEventAvroModel> {

    private final OrderApplicationService orderApplicationService;

    @Override
    @KafkaListener(id = "PaymentEventListener",
            groupId = "${order-service.kafka.group-id}",
            topics = "${order-service.kafka.topics.payment-event}")
    public void receive(@Payload List<PaymentEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(PaymentEventAvroModel event) {
        switch (event.getType()) {
            case PAYMENT_COMPLETED -> handlePaymentCompletedAvroEvent(((PaymentCompletedAvroEvent) event.getPayload()));
            case PAYMENT_CANCELLED -> handlePaymentCancelledAvroEvent((PaymentCancelledAvroEvent) event.getPayload());
            case PAYMENT_FAILED -> handlePaymentFailedAvroEvent((PaymentFailedAvroEvent) event.getPayload());
        }
    }

    private void handlePaymentCompletedAvroEvent(PaymentCompletedAvroEvent event) {
        var orderId = new OrderId(event.getOrderId());
        orderApplicationService.payOrder(orderId);
    }

    private void handlePaymentCancelledAvroEvent(PaymentCancelledAvroEvent event) {
        var orderId = new OrderId(event.getOrderId());
        orderApplicationService.cancelOrder(orderId, null);
    }

    private void handlePaymentFailedAvroEvent(PaymentFailedAvroEvent event) {
        var orderId = new OrderId(event.getOrderId());
        orderApplicationService.cancelOrder(orderId, event.getFailureMessages());
    }

    @Override
    protected String getMessageTypeName() {
        return "paymentEvent";
    }
}