package pl.kopytka.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID productId,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice
) {
}