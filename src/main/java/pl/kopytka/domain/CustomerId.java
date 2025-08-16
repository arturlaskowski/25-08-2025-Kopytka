package pl.kopytka.domain;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record CustomerId(UUID customerId) {

    public static CustomerId newOne() {
        return new CustomerId(UUID.randomUUID());
    }

    public UUID id() {
        return customerId;
    }
}
