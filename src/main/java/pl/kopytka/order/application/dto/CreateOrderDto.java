package pl.kopytka.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record CreateOrderDto(
        @NotNull UUID customerId,
        @NotNull @Min(0) BigDecimal price,
        @Valid @NotNull List<CreateOrderItemDto> basketItems,
        @Valid CreateOrderAddressDto address
) {
}