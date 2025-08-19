package pl.kopytka.common.outbox;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.core.KafkaTemplate;
import pl.kopytka.avro.customer.CustomerEventAvroModel;

/**
 * Test implementation of OutboxPublisher for integration testing.
 * This publisher handles customer events by publishing them to Kafka.
 */
@TestComponent
@Slf4j
@Setter
class TestOutboxPublisher extends OutboxPublisher {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private boolean shouldFail = false;

    @Override
    protected void publish(OutboxEntry entry) {
        if (shouldFail) {
            throw new RuntimeException("Simulated publishing failure");
        }

        if (entry.getMessageType().equals("customer.created")) {
            publishCustomerEvent(entry);
        } else {
            throw new IllegalArgumentException("Unknown message type: " + entry.getMessageType());
        }
    }

    private void publishCustomerEvent(OutboxEntry entry) {
        // Deserialize to validate the payload format
        deserializeAvroPayload(entry, CustomerEventAvroModel.class);

        String topic = "customer-events";
        String key = entry.getMessageKey();
        String payload = entry.getPayload(); // Send as JSON string for easier testing

        log.info("Publishing customer event to topic: {}, key: {}", topic, key);
        kafkaTemplate.send(topic, key, payload);
    }
}
