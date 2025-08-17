package pl.kopytka.common.inbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.BaseIntegrationTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the InboxCleaner functionality.
 * Tests the cleanup of old processed inbox entries.
 */
@AcceptanceTest
class InboxCleanupIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxCleaner inboxCleaner;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("kopytka.scheduling.enabled", () -> "true");
        registry.add("inbox.cleaner.retention-days", () -> "7");
        registry.add("inbox.cleaner.fixed-delay", () -> "100");
    }

    @BeforeEach
    void setUp() {
        inboxRepository.deleteAll();
    }

    @Test
    void shouldCleanupOldProcessedEntries() {
        // Given - Entries with different ages
        String messageId1 = UUID.randomUUID().toString();
        String messageId2 = UUID.randomUUID().toString();
        String messageId3 = UUID.randomUUID().toString();

        // Create old processed entry (should be cleaned up)
        inboxService.processIfNotExists(messageId1, () -> {
            // Simulate processing
        });
        
        InboxEntry oldEntry = inboxRepository.findAll().getFirst();
        var oldProcessedAt = Instant.now().minus(10, ChronoUnit.DAYS);
        setProcessedAt(oldEntry, oldProcessedAt);
        inboxRepository.save(oldEntry);

        // Create recent processed entry (should not be cleaned up)
        inboxService.processIfNotExists(messageId2, () -> {
            // Simulate processing
        });
        // Recent entry will have current processedAt timestamp

        // Create another recent entry
        inboxService.processIfNotExists(messageId3, () -> {
            // Simulate processing
        });

        // Verify initial state has all three entries
        assertThat(inboxRepository.findAll()).hasSize(3);

        // When - Run cleanup process
        inboxCleaner.cleanup();

        // Then - Only old processed entry should be removed, others preserved
        var remainingEntries = inboxRepository.findAll();
        assertThat(remainingEntries)
                .hasSize(2)
                .extracting(InboxEntry::getMessageId)
                .containsExactlyInAnyOrder(messageId2, messageId3)
                .doesNotContain(messageId1);
    }

    @Test
    void shouldNotCleanupRecentEntries() {
        // Given - Recent processed entries
        String messageId1 = UUID.randomUUID().toString();
        String messageId2 = UUID.randomUUID().toString();
        
        inboxService.processIfNotExists(messageId1, () -> {
            // Simulate processing
        });
        
        inboxService.processIfNotExists(messageId2, () -> {
            // Simulate processing
        });

        // Verify initial state
        assertThat(inboxRepository.findAll()).hasSize(2);

        // When - Run cleanup process
        inboxCleaner.cleanup();

        // Then - Recent entries should remain untouched
        var remainingEntries = inboxRepository.findAll();
        assertThat(remainingEntries)
                .hasSize(2)
                .extracting(InboxEntry::getMessageId)
                .containsExactlyInAnyOrder(messageId1, messageId2);
    }

    @Test
    void shouldHandleEmptyInboxDuringCleanup() {
        // Given - Empty inbox
        assertThat(inboxRepository.findAll()).isEmpty();

        // When - Run cleanup process
        inboxCleaner.cleanup();

        // Then - Should complete without errors and remain empty
        assertThat(inboxRepository.findAll()).isEmpty();
    }

    // Helper method to set processedAt using reflection since it's not publicly settable
    private void setProcessedAt(InboxEntry entry, Instant processedAt) {
        try {
            var field = InboxEntry.class.getDeclaredField("processedAt");
            field.setAccessible(true);
            field.set(entry, processedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set processedAt", e);
        }
    }
}
