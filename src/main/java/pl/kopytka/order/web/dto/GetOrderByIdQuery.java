package pl.kopytka.order.web.dto;


import pl.kopytka.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetOrderByIdQuery(
        UUID id,
        UUID customerId,
        BigDecimal price,
        OrderStatus status,
        List<GetOrderItemDto> basketItems,
        GetOrderAddressDto address) {
}

