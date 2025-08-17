package pl.kopytka.payment.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record MakePaymentRequest(
    @NotNull UUID orderId,
    @NotNull UUID customerId,
    @NotNull @Positive BigDecimal price
) {}
