package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.PaymentEventAvroModel;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
class PaymentOutboxPublisher extends OutboxPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, PaymentEventAvroModel> kafkaProducer;

    @Override
    protected void publish(OutboxEntry entry) {
        String messageType = entry.getMessageType();

        if (PaymentOutboxWriter.MESSAGE_TYPE_PAYMENT_COMPLETED.equals(messageType) ||
                PaymentOutboxWriter.MESSAGE_TYPE_PAYMENT_FAILED.equals(messageType) ||
                PaymentOutboxWriter.MESSAGE_TYPE_PAYMENT_CANCELED.equals(messageType)) {
            PaymentEventAvroModel avro = deserializeAvroPayload(entry, PaymentEventAvroModel.class);
            publishToKafka(avro, entry.getMessageKey());
        } else {
            log.warn("Unknown message type in outbox entry: {}", messageType);
        }
    }

    void publishToKafka(PaymentEventAvroModel avro, String messageKey) {
        kafkaProducer.send(
                topicsConfigData.getPaymentEvent(),
                messageKey,
                avro
        );
    }
}
