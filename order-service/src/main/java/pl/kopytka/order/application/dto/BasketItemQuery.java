package pl.kopytka.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BasketItemQuery(
    UUID productId,
    BigDecimal price,
    Integer quantity,
    BigDecimal totalPrice
) {}
