package pl.kopytka.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Quantity(Integer value) {

    public Quantity {
        if (value == null) {
            throw new IllegalArgumentException("Number of elements cannot be null");
        }
        if (value < 1) {
            throw new IllegalArgumentException("Number of elements must be at least 1");
        }
    }
}
