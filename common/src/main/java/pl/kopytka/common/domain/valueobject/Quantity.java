package pl.kopytka.common.domain.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
