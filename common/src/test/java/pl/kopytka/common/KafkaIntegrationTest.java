package pl.kopytka.common;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Base class for Kafka integration tests.
 * Provides common infrastructure for testing Kafka event publishing in acceptance tests.
 * <p>
 * This class configures a Kafka consumer with proper error handling and deserialization
 * to capture and verify events published to Kafka topics during tests.
 * <p>
 * The consumer is configured with:
 * - StringDeserializer for both keys and values
 * - ErrorHandlingDeserializer to gracefully handle deserialization issues
 * - earliest offset reset to ensure we capture all events from the beginning
 */
public abstract class KafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;

    protected KafkaMessageListenerContainer<String, String> container;
    protected BlockingQueue<ConsumerRecord<String, String>> records;

    /**
     * Sets up a Kafka message listener container to capture messages sent to the specified topic.
     *
     * @param topicName The name of the topic to listen to
     */
    protected void setupKafkaConsumer(String topicName) {
        records = new LinkedBlockingQueue<>();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-consumer", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Use ByteArrayDeserializer for more flexibility with message formats
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        // Enable auto-commit to avoid issues with consumer rebalancing
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");

        // Set a higher receive buffer size
        consumerProps.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, 65536);

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(topicName);

        // Configure error handling
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);

        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();

        // Wait until container is ready
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    /**
     * Stop and clean up the Kafka message listener container.
     */
    @AfterEach
    protected void tearDownKafkaConsumer() {
        if (container != null) {
            container.stop();
        }
    }
}
