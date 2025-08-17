package pl.kopytka.order.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull UUID customerId,
    @NotNull UUID restaurantId,
    @NotNull @Valid OrderAddressRequest deliveryAddress,
    @NotNull @Positive BigDecimal price,
    @NotNull @NotEmpty List<@Valid BasketItemRequest> basketItems
) {}
