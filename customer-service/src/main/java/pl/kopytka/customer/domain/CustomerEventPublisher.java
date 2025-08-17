package pl.kopytka.customer.domain;


import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.customer.domain.event.CustomerEvent;

public interface CustomerEventPublisher extends DomainEventPublisher<CustomerEvent> {
    void publish(CustomerEvent event);
}
