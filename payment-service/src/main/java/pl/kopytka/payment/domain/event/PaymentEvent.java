package pl.kopytka.payment.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kopytka.common.domain.event.DomainEvent;
import pl.kopytka.payment.domain.Payment;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public abstract class PaymentEvent implements DomainEvent {
    private final Payment payment;
    private final Instant createdAt;

    PaymentEvent(Payment payment) {
        this.payment = payment;
        this.createdAt = Instant.now();
    }
}
