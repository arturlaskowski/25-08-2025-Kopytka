package pl.kopytka.order.application.integration.payment;

import java.util.UUID;

public record PaymentResult(
        UUID paymentId,
        boolean success,
        String errorMessage
) {
}
