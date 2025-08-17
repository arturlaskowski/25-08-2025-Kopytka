package pl.kopytka.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MakePaymentCommand(
    UUID orderId,
    UUID customerId,
    BigDecimal price
) {}
