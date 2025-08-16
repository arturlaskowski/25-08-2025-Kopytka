package pl.kopytka.order.application.dto;


import pl.kopytka.order.domain.OrderStatus;

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