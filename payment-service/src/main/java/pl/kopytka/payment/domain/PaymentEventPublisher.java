package pl.kopytka.payment.domain;


import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.payment.domain.event.PaymentEvent;

public interface PaymentEventPublisher extends DomainEventPublisher<PaymentEvent>  {
    void publish(PaymentEvent event);
}
