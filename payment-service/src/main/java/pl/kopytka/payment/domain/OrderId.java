package pl.kopytka.payment.domain;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record OrderId(UUID orderId) {

    public static OrderId newOne() {
        return new OrderId(UUID.randomUUID());
    }

    public UUID id() {
        return orderId;
    }
}
