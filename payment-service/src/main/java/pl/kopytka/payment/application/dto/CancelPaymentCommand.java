package pl.kopytka.payment.application.dto;

import java.util.UUID;

public record CancelPaymentCommand(
    UUID orderId,
    UUID customerId
) {}
