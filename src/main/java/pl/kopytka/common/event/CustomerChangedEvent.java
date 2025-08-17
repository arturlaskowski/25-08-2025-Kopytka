package pl.kopytka.common.event;

import java.time.Instant;
import java.util.UUID;

public record CustomerChangedEvent(
        UUID customerId,
        String email,
        Instant occurredAt
) {
    public CustomerChangedEvent(UUID customerId, String email) {
        this(customerId, email, Instant.now());
    }
}