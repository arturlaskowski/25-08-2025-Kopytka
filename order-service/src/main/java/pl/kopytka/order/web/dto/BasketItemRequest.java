package pl.kopytka.order.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record BasketItemRequest(
        @NotNull UUID productId,
        @NotNull @Positive BigDecimal price,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal totalPrice
) {
}
