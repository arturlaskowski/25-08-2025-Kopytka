package pl.kopytka.order.web.dto;


import pl.kopytka.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPageQuery(UUID orderId, Instant createAt, OrderStatus status, BigDecimal price) {
}
