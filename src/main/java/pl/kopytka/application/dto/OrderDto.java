package pl.kopytka.application.dto;


import pl.kopytka.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID customerId,
        BigDecimal price,
        OrderStatus status,
        List<OrderItemDto> basketItems,
        OrderAddressDto address) {
}