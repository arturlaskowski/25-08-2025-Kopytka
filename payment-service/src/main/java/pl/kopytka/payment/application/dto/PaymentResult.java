package pl.kopytka.payment.application.dto;

import java.util.UUID;

public record PaymentResult(
        UUID paymentId,
        boolean success,
        String errorMessage
) {
}
