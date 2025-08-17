package pl.kopytka.payment.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelPaymentRequest(
    @NotNull UUID orderId,
    @NotNull UUID customerId
) {}
