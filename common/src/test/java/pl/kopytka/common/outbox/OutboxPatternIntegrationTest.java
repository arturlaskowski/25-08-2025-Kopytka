package pl.kopytka.common.outbox;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.KafkaIntegrationTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the Outbox Pattern mechanism.
 * These tests verify the complete flow:
 * 1. Saving events to the outbox table
 * 2. Processing events by the OutboxPublisher
 * 3. Publishing events to Kafka
 * 4. Verifying database state changes
 */
@AcceptanceTest(topics = {"customer-events", "test-topic"})
@Import(OutboxTestConfiguration.class)
class OutboxPatternIntegrationTest extends KafkaIntegrationTest {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private TestOutboxPublisher testOutboxPublisher;

    private static final String CUSTOMER_EVENTS_TOPIC = "customer-events";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("kopytka.scheduling.enabled", () -> "true");
        registry.add("outbox.publisher.fixed-delay", () -> "100");
    }

    @BeforeEach
    void setUp() {
        setupKafkaConsumer(CUSTOMER_EVENTS_TOPIC);
        outboxRepository.deleteAll();
        records.clear();
    }

    @Test
    void shouldSaveEventToOutboxAndPublishToKafka() {
        // Given
        UUID customerId = UUID.randomUUID();
        CustomerEventAvroModel customerEvent = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId)
                .setEmail("waldemar@kiepski.pl")
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(Instant.now())
                .build();

        // When - Save event to outbox
        outboxService.save("customer.created", customerId.toString(), customerEvent);

        // Then - Verify event is saved to outbox with NEW status
        var outboxEntries = outboxRepository.findAll();
        assertThat(outboxEntries).hasSize(1);
        
        OutboxEntry savedEntry = outboxEntries.getFirst();
        assertThat(savedEntry)
                .extracting(
                        OutboxEntry::getMessageType,
                        OutboxEntry::getMessageKey,
                        OutboxEntry::getStatus,
                        OutboxEntry::getProcessedAt
                )
                .containsExactly(
                        "customer.created",
                        customerId.toString(),
                        OutboxStatus.NEW,
                        null
                );
        assertThat(savedEntry.getPayload()).contains(customerId.toString());

        // When - Trigger outbox processing (simulating scheduled task)
        testOutboxPublisher.process();

        // Then - Verify event status is updated to PUBLISHED
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var updatedEntry = outboxRepository.findById(savedEntry.getId()).orElseThrow();
            assertThat(updatedEntry)
                    .extracting(OutboxEntry::getStatus, OutboxEntry::getProcessedAt)
                    .satisfies(values -> {
                        assertThat(values.get(0)).isEqualTo(OutboxStatus.PUBLISHED);
                        assertThat(values.get(1)).isNotNull();
                    });
        });

        // And - Verify event is published to Kafka
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(records).hasSize(1);
            
            ConsumerRecord<String, String> consumerRecord = records.poll();
            assertThat(consumerRecord)
                    .isNotNull()
                    .extracting(
                            ConsumerRecord::topic,
                            ConsumerRecord::key
                    )
                    .containsExactly(CUSTOMER_EVENTS_TOPIC, customerId.toString());
            
            assertThat(consumerRecord.value())
                    .contains(customerId.toString())
                    .contains("CUSTOMER_CREATED");
        });
    }

    @Test
    void shouldMarkEventAsFailedWhenPublishingFails() {
        // Given - A customer event and a publisher configured to fail
        UUID customerId = UUID.randomUUID();
        CustomerEventAvroModel customerEvent = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId)
                .setEmail("ferdynand@kiepski.pl")
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(Instant.now())
                .build();

        outboxService.save("customer.created", customerId.toString(), customerEvent);
        testOutboxPublisher.setShouldFail(true);
        var savedEntry = outboxRepository.findAll().getFirst();

        // When - Process outbox (should fail)
        testOutboxPublisher.process();

        // Then - Verify event status is updated to FAILED with processed timestamp
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var updatedEntry = outboxRepository.findById(savedEntry.getId()).orElseThrow();
            assertThat(updatedEntry)
                    .extracting(OutboxEntry::getStatus, OutboxEntry::getProcessedAt)
                    .satisfies(values -> {
                        assertThat(values.get(0)).isEqualTo(OutboxStatus.FAILED);
                        assertThat(values.get(1)).isNotNull();
                    });
        });

        // And - Verify no event is published to Kafka
        await().during(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(records).isEmpty());
    }

    @Test
    void shouldRetrieveOnlyNewEntriesInCorrectOrder() {
        // Given - Create entries with different statuses and timestamps
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();
        UUID customerId3 = UUID.randomUUID();

        CustomerEventAvroModel event1 = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId1)
                .setEmail("arnold@boczek.pl")
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(Instant.now())
                .build();

        // Save and publish first event
        outboxService.save("customer.created", customerId1.toString(), event1);
        OutboxEntry entry1 = outboxRepository.findAll().getFirst();
        entry1.publish();
        outboxRepository.save(entry1);

        // Save second event (should be NEW) with explicit timestamp difference
        Instant secondEventTime = Instant.now().minus(5, ChronoUnit.SECONDS);
        CustomerEventAvroModel event2 = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId2)
                .setEmail("edzio@listonosz")
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(secondEventTime)
                .build();
        outboxService.save("customer.created", customerId2.toString(), event2);

        // Save third event (should be NEW) with explicit timestamp difference
        Instant thirdEventTime = Instant.now().minus(1, ChronoUnit.SECONDS);
        CustomerEventAvroModel event3 = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId3)
                .setEmail("marian@pazdzioch.pl")
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(thirdEventTime)
                .build();
        outboxService.save("customer.created", customerId3.toString(), event3);

        // When - Retrieve NEW entries using repository query
        var newEntries = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        // Then - Should only return NEW entries in chronological order
        assertThat(newEntries)
                .hasSize(2)
                .extracting(OutboxEntry::getMessageKey)
                .containsExactly(customerId2.toString(), customerId3.toString());
        
        assertThat(newEntries.get(0).getCreatedAt())
                .isBefore(newEntries.get(1).getCreatedAt());
    }
}
