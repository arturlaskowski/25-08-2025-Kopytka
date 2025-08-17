package pl.kopytka.order.application.integration.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record MakePaymentRequest(
    UUID orderId,
    UUID customerId,
    BigDecimal price
) {}
