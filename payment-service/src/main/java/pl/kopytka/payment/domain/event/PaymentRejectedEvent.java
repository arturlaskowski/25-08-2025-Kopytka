package pl.kopytka.payment.domain.event;

import pl.kopytka.payment.domain.Payment;

public class PaymentRejectedEvent extends PaymentEvent {
    public PaymentRejectedEvent(Payment payment) {
        super(payment);
    }
}
