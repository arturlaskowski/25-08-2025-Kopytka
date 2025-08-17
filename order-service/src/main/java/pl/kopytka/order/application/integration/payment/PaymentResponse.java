package pl.kopytka.order.application.integration.payment;

import java.util.UUID;

/**
 * Represents the response from the payment service
 */
public record PaymentResponse(
        UUID paymentId,
        boolean success,
        String errorMessage
) {
}
