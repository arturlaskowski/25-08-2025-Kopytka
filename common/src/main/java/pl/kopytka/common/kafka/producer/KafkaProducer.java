package pl.kopytka.common.kafka.producer;


import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer<K extends Serializable, V extends SpecificRecordBase> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public void send(String topicName, K key, V message) {
        log.info("Sending message={} to topic={}", message, topicName);
        try {
            CompletableFuture<SendResult<K, V>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);
            String resourceId = key.toString();

            kafkaResultFuture.whenComplete(createCallback(topicName, message, resourceId));
        } catch (KafkaException e) {
            log.error("Error on kafka producer with key: {}, message: {} and exception: {}", key, message,
                    e.getMessage());
            throw new KafkaProducerException("Error on kafka producer with key: " + key + " and message: " + message);
        }
    }

    private BiConsumer<SendResult<K, V>, Throwable> createCallback(String topicName, V message, String resourceId) {
        return (result, ex) -> {
            if (ex != null) {
                log.error("Error while sending message for resource id: {} with payload: {} to topic: {}",
                        resourceId, message.toString(), topicName, ex);
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from Kafka for resource id: {}. " +
                                "Topic: {} Partition: {} Offset: {} Timestamp: {}",
                        resourceId,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp());
            }
        };
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }
}
