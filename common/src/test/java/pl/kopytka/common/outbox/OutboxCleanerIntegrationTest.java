package pl.kopytka.common.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.BaseIntegrationTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the OutboxCleaner functionality.
 * Tests the cleanup of old published outbox entries.
 */
@AcceptanceTest
class OutboxCleanerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxCleaner outboxCleaner;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("kopytka.scheduling.enabled", () -> "true");
        registry.add("outbox.cleaner.retention-days", () -> "7");
        registry.add("outbox.cleaner.fixed-delay", () -> "100");
    }

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
    }

    @Test
    void shouldCleanupOldPublishedEntries() {
        // Given - Entries with different statuses and ages
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();
        UUID customerId3 = UUID.randomUUID();

        // Create old published entry (should be cleaned up)
        CustomerEventAvroModel oldEvent = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId1)
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setEmail("old.customer@example.com")
                .setCreatedAt(Instant.now())
                .build();
        outboxService.save("customer.created", customerId1.toString(), oldEvent);
        
        OutboxEntry oldEntry = outboxRepository.findAll().getFirst();
        oldEntry.publish();
        var oldProcessedAt = Instant.now().minus(10, ChronoUnit.DAYS);
        setProcessedAt(oldEntry, oldProcessedAt);
        outboxRepository.save(oldEntry);

        // Create recent published entry (should not be cleaned up)
        CustomerEventAvroModel recentEvent = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId2)
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setEmail("recent.customer@example.com")
                .setCreatedAt(Instant.now())
                .build();
        outboxService.save("customer.created", customerId2.toString(), recentEvent);
        
        OutboxEntry recentEntry = outboxRepository.findAll().stream()
                .filter(e -> e.getMessageKey().equals(customerId2.toString()))
                .findFirst().orElseThrow();
        recentEntry.publish();
        outboxRepository.save(recentEntry);

        // Create NEW entry (should not be cleaned up)
        CustomerEventAvroModel newEvent = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId3)
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setEmail("new.customer@example.com")
                .setCreatedAt(Instant.now())
                .build();
        outboxService.save("customer.created", customerId3.toString(), newEvent);

        // Verify initial state has all three entries
        assertThat(outboxRepository.findAll()).hasSize(3);

        // When - Run cleanup process
        outboxCleaner.cleanup();

        // Then - Only old published entry should be removed, others preserved
        var remainingEntries = outboxRepository.findAll();
        assertThat(remainingEntries)
                .hasSize(2)
                .extracting(OutboxEntry::getMessageKey)
                .containsExactlyInAnyOrder(customerId2.toString(), customerId3.toString())
                .doesNotContain(customerId1.toString());
    }

    @Test
    void shouldNotCleanupFailedEntries() {
        // Given - A failed entry that is old enough to be cleaned if it were published
        UUID customerId = UUID.randomUUID();
        CustomerEventAvroModel event = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId)
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setEmail("failed.customer@example.com")
                .setCreatedAt(Instant.now())
                .build();
        
        outboxService.save("customer.created", customerId.toString(), event);
        
        OutboxEntry entry = outboxRepository.findAll().getFirst();
        entry.fail();
        var oldProcessedAt = Instant.now().minus(10, ChronoUnit.DAYS);
        setProcessedAt(entry, oldProcessedAt);
        outboxRepository.save(entry);

        // When - Run cleanup process
        outboxCleaner.cleanup();

        // Then - Failed entry should remain untouched
        var remainingEntries = outboxRepository.findAll();
        assertThat(remainingEntries)
                .hasSize(1)
                .first()
                .extracting(OutboxEntry::getStatus)
                .isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    void shouldNotCleanupNewEntries() {
        // Given - A NEW entry that hasn't been processed yet
        UUID customerId = UUID.randomUUID();
        CustomerEventAvroModel event = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId)
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setEmail("new.entry@example.com")
                .setCreatedAt(Instant.now())
                .build();
        
        outboxService.save("customer.created", customerId.toString(), event);

        // When - Run cleanup process
        outboxCleaner.cleanup();

        // Then - NEW entry should remain untouched
        var remainingEntries = outboxRepository.findAll();
        assertThat(remainingEntries)
                .hasSize(1)
                .first()
                .extracting(OutboxEntry::getStatus)
                .isEqualTo(OutboxStatus.NEW);
    }

    // Helper method to set processedAt using reflection since it's not publicly settable
    private void setProcessedAt(OutboxEntry entry, Instant processedAt) {
        try {
            var field = OutboxEntry.class.getDeclaredField("processedAt");
            field.setAccessible(true);
            field.set(entry, processedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set processedAt", e);
        }
    }
}
