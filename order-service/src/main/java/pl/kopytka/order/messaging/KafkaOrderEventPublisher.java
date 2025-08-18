package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kopytka.order.domain.event.*;
import pl.kopytka.order.saga.OrderSagaDispatcher;

@Component
@RequiredArgsConstructor
@Slf4j
class KafkaOrderEventPublisher {

    private final OrderSagaDispatcher orderSagaDispatcher;

    // Gdyby orchestrator sagi znajdował się w innym module, te eventy byłyby normalnie publikowane na Kafkę.
    // Jednak ponieważ znajduje się w tym samym module, można bezpośrednio wywołać komponent sagi.
    @TransactionalEventListener
    @Async
    public void publish(OrderEvent event) {
        switch (event) {
            case OrderCreatedEvent createdEvent -> orderSagaDispatcher.handle(createdEvent);
            case OrderPaidEvent paidEvent -> orderSagaDispatcher.handle(paidEvent);
            case OrderCanceledEvent canceledEvent -> orderSagaDispatcher.handle(canceledEvent);
            case OrderApprovedEvent approvedEvent -> orderSagaDispatcher.handle(approvedEvent);
            case OrderCancelInitiatedEvent cancelInitiatedEvent -> orderSagaDispatcher.handle(cancelInitiatedEvent);
            default ->
                    throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getSimpleName());
        }
    }
}