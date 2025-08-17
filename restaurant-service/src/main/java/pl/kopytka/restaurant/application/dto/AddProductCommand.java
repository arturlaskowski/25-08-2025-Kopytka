package pl.kopytka.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AddProductCommand(UUID restaurantId, String productName, BigDecimal price) {
}
