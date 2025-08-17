package pl.kopytka.order.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GetOrderItemDto(
        UUID productId,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice
) {
}
