package pl.kopytka.payment.domain.event;


import pl.kopytka.payment.domain.Payment;

public class PaymentCanceledEvent extends PaymentEvent {

    public PaymentCanceledEvent(Payment payment) {
        super(payment);
    }
}