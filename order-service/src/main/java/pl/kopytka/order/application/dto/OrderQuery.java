package pl.kopytka.order.application.dto;


import pl.kopytka.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderQuery(
    UUID id,
    UUID customerId,
    UUID restaurantId,
    BigDecimal price,
    OrderStatus status,
    Instant creationDate,
    String failureMessages,
    List<BasketItemQuery> basketItems,
    OrderAddressQuery deliveryAddress
) {}
