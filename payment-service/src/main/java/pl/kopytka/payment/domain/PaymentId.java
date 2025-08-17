package pl.kopytka.payment.domain;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record PaymentId(UUID paymentId) {

    public static PaymentId newOne() {
        return new PaymentId(UUID.randomUUID());
    }

    public UUID id() {
        return paymentId;
    }
}
