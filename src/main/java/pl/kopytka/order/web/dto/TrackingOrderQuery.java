package pl.kopytka.order.web.dto;

import pl.kopytka.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TrackingOrderQuery(UUID orderId, OrderStatus status, BigDecimal price) {
}