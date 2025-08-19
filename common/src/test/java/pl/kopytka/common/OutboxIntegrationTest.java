package pl.kopytka.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxRepository;
import pl.kopytka.common.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base class for Outbox Pattern integration tests.
 * Provides common infrastructure for testing outbox events saved to database in acceptance tests.
 * <p>
 * This class provides utilities for verifying that events are properly saved to the outbox table
 * when using the Outbox pattern instead of directly publishing to Kafka.
 * <p>
 * The outbox entries can be verified for:
 * - Message type
 * - Message key
 * - Status (NEW, PUBLISHED, FAILED)
 * - Payload content
 * - Creation and processing timestamps
 */
public abstract class OutboxIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected OutboxRepository outboxRepository;

    @BeforeEach
    void cleanupOutboxEntries() {
        clearOutboxEntries();
    }

    /**
     * Find outbox entry by message type and key.
     *
     * @param messageType The message type to search for
     * @param messageKey  The message key to search for
     * @return Optional containing the outbox entry if found
     */
    protected Optional<OutboxEntry> findOutboxEntry(String messageType, String messageKey) {
        return outboxRepository.findAll().stream()
                .filter(entry -> messageType.equals(entry.getMessageType()) && messageKey.equals(entry.getMessageKey()))
                .findFirst();
    }

    /**
     * Find all outbox entries with the specified message type.
     *
     * @param messageType The message type to search for
     * @return List of outbox entries with the specified message type
     */
    protected List<OutboxEntry> findOutboxEntriesByMessageType(String messageType) {
        return outboxRepository.findAll().stream()
                .filter(entry -> messageType.equals(entry.getMessageType()))
                .toList();
    }

    /**
     * Find all outbox entries with the specified status.
     *
     * @param status The status to search for
     * @return List of outbox entries with the specified status
     */
    protected List<OutboxEntry> findOutboxEntriesByStatus(OutboxStatus status) {
        return outboxRepository.findAll().stream()
                .filter(entry -> status.equals(entry.getStatus()))
                .toList();
    }

    /**
     * Get the count of outbox entries with NEW status.
     *
     * @return Number of NEW outbox entries
     */
    protected long getNewOutboxEntriesCount() {
        return outboxRepository.findAll().stream()
                .filter(entry -> OutboxStatus.NEW.equals(entry.getStatus()))
                .count();
    }

    /**
     * Get all outbox entries from the database.
     *
     * @return List of all outbox entries
     */
    protected List<OutboxEntry> getAllOutboxEntries() {
        return outboxRepository.findAll();
    }

    /**
     * Clear all outbox entries from the database.
     * Useful for test cleanup.
     */
    protected void clearOutboxEntries() {
        outboxRepository.deleteAll();
    }
}
