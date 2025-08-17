package pl.kopytka.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductProjection(
    UUID id,
    String name,
    BigDecimal price,
    boolean available
) {
}
