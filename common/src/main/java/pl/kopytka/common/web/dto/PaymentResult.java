package pl.kopytka.common.web.dto;

import java.util.UUID;

public record PaymentResult(
        UUID paymentId,
        boolean success,
        String errorMessage
) {
}
