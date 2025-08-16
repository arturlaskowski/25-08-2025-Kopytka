package pl.kopytka.order.domain;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ProductId(UUID productId) {

    public static ProductId newOne() {
        return new ProductId(UUID.randomUUID());
    }

    public UUID id() {
        return productId;
    }
}
