package pl.kopytka.order.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
