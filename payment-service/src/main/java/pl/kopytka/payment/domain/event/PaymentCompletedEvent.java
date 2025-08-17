package pl.kopytka.payment.domain.event;


import pl.kopytka.payment.domain.Payment;

public class PaymentCompletedEvent extends PaymentEvent {
    public PaymentCompletedEvent(Payment payment) {
        super(payment);
    }
}
