package pl.kopytka.order.domain;


import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.order.domain.event.OrderEvent;

public interface OrderEventPublisher extends DomainEventPublisher<OrderEvent> {
    void publish(OrderEvent event);
}
