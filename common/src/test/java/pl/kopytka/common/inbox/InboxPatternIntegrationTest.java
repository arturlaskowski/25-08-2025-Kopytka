package pl.kopytka.common.inbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.KafkaIntegrationTest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the Inbox Pattern mechanism using Kafka.
 * These tests verify the complete flow:
 * 1. Processing messages from Kafka and ensuring idempotency
 * 2. Preventing duplicate message processing from Kafka
 * 3. Handling concurrent message processing via Kafka
 * 4. Database consistency and transaction management
 */
@AcceptanceTest(topics = {"test-events"})
@Import(InboxTestConfiguration.class)
class InboxPatternIntegrationTest extends KafkaIntegrationTest {

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private TestMessageProcessor testMessageProcessor;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TEST_EVENTS_TOPIC = "test-events";

    @BeforeEach
    void setUp() {
        inboxRepository.deleteAll();
        testMessageProcessor.reset();
    }

    @Test
    void shouldProcessMessageOnlyOnceWhenReceivedFromKafka() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String messageContent = "test-message-content";

        // When - Send message to Kafka
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId, messageContent);

        // Then - Wait for message to be processed automatically by the consumer
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testMessageProcessor.getProcessedCount() > 0);

        // And - Verify message was processed exactly once
        assertThat(testMessageProcessor.getProcessedCount()).isEqualTo(1);
        assertThat(testMessageProcessor.getProcessedMessages()).contains(messageContent);

        // And - Verify inbox entry was created
        var inboxEntries = inboxRepository.findAll();
        assertThat(inboxEntries).hasSize(1);

        InboxEntry entry = inboxEntries.getFirst();
        assertThat(entry.getMessageId()).isEqualTo(messageId);
        assertThat(entry.getProcessedAt()).isNotNull();
    }

    @Test
    void shouldNotProcessDuplicateMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String messageContent = "test-message-content";

        // When - Send same message ID twice to Kafka
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId, messageContent);
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId, messageContent + "-duplicate");

        // Then - Wait for at least one message to be processed
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testMessageProcessor.getProcessedCount() > 0);

        // And - Give additional time to ensure duplicate processing would have occurred if not prevented
        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> {
                    // Check that count remains 1 for a stable period
                    int currentCount = testMessageProcessor.getProcessedCount();
                    int inboxCount = inboxRepository.findAll().size();
                    return currentCount == 1 && inboxCount == 1;
                });

        // And - Verify only first message was processed (idempotency)
        assertThat(testMessageProcessor.getProcessedCount()).isEqualTo(1);
        assertThat(testMessageProcessor.getProcessedMessages()).hasSize(1);
        assertThat(testMessageProcessor.getProcessedMessages().getFirst()).isEqualTo(messageContent);

        // And - Verify only one inbox entry exists
        var inboxEntries = inboxRepository.findAll();
        assertThat(inboxEntries).hasSize(1);
    }

    @Test
    void shouldProcessDifferentMessagesIndependently() {
        // Given
        String messageId1 = UUID.randomUUID().toString();
        String messageId2 = UUID.randomUUID().toString();
        String messageId3 = UUID.randomUUID().toString();
        String content1 = "message-1";
        String content2 = "message-2";
        String content3 = "message-3";

        // When - Send different messages to Kafka
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId1, content1);
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId2, content2);
        kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId3, content3);

        // Then - Wait for all messages to be processed
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testMessageProcessor.getProcessedCount() >= 3);

        // And - Verify all messages were processed
        assertThat(testMessageProcessor.getProcessedCount()).isEqualTo(3);
        assertThat(testMessageProcessor.getProcessedMessages())
                .containsExactlyInAnyOrder(content1, content2, content3);

        // And - Verify all inbox entries were created
        var inboxEntries = inboxRepository.findAll();
        assertThat(inboxEntries).hasSize(3);
        assertThat(inboxEntries)
                .extracting(InboxEntry::getMessageId)
                .containsExactlyInAnyOrder(messageId1, messageId2, messageId3);
    }

    @Test
    void shouldHandleConcurrentProcessingOfSameMessage() {
        // Given
        String messageId = UUID.randomUUID().toString();
        String messageContent = "concurrent-test-message";

        // When - Send multiple messages with same ID quickly to simulate race condition
        for (int i = 0; i < 5; i++) {
            kafkaTemplate.send(TEST_EVENTS_TOPIC, messageId, messageContent + "-" + i);
        }

        // Then - Wait for at least one message to be processed
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> testMessageProcessor.getProcessedCount() > 0);

        // And - Wait for processing to stabilize and ensure no additional processing occurs
        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> {
                    // Verify business logic processed only one message (idempotency)
                    int processedCount = testMessageProcessor.getProcessedCount();
                    int inboxCount = inboxRepository.findAll().size();
                    return processedCount == 1 && inboxCount == 1;
                });

        // But - Verify business logic processed only one message (idempotency)
        assertThat(testMessageProcessor.getProcessedCount()).isEqualTo(1);
        assertThat(testMessageProcessor.getProcessedMessages()).hasSize(1);

        // And - Verify only one inbox entry exists
        var inboxEntries = inboxRepository.findAll();
        assertThat(inboxEntries).hasSize(1);
        assertThat(inboxEntries.getFirst().getMessageId()).isEqualTo(messageId);
    }
}
