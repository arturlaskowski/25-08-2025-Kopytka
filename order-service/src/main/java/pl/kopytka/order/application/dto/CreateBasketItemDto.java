package pl.kopytka.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBasketItemDto(
    @NotNull UUID productId,
    @NotNull @Positive BigDecimal price,
    @NotNull @Positive Integer quantity,
    @NotNull BigDecimal totalPrice
) {}
