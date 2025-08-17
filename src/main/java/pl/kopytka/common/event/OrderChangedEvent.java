package pl.kopytka.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderChangedEvent(
        UUID orderId,
        UUID customerId,
        String orderStatus,
        BigDecimal amount,
        Instant orderCreateAt,
        Instant occurredAt
) {
    public OrderChangedEvent(UUID orderId, UUID customerId, String status, BigDecimal amount, Instant orderCreateAt) {
        this(orderId, customerId, status, amount, orderCreateAt, Instant.now());
    }
}