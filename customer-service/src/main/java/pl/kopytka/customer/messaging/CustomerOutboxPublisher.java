package pl.kopytka.customer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
class CustomerOutboxPublisher extends OutboxPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, CustomerEventAvroModel> kafkaProducer;

    @Override
    protected void publish(OutboxEntry entry) {
        if (CustomerOutboxWriter.MESSAGE_TYPE_CUSTOMER_CREATED.equals(entry.getMessageType())) {
            CustomerEventAvroModel avro = deserializeAvroPayload(entry, CustomerEventAvroModel.class);
            publishToKafka(avro, entry.getMessageKey());
        }
    }

    void publishToKafka(CustomerEventAvroModel avro, String messageKey) {
        kafkaProducer.send(
                topicsConfigData.getCustomerEvent(),
                messageKey,
                avro
        );
    }
}
