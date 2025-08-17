package pl.kopytka.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    @NotNull UUID customerId,
    @NotNull @Valid CreateOrderAddressDto deliveryAddress,
    @NotNull @Positive BigDecimal price,
    @NotNull @NotEmpty List<@Valid CreateBasketItemDto> basketItems
) {}
