package pl.kopytka.customer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.outbox.OutboxService;
import pl.kopytka.customer.domain.CustomerEventPublisher;
import pl.kopytka.customer.domain.event.CustomerCreatedEvent;
import pl.kopytka.customer.domain.event.CustomerEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class CustomerOutboxWriter implements CustomerEventPublisher {

    static final String MESSAGE_TYPE_CUSTOMER_CREATED = "customerCreated";
    private final OutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(CustomerEvent event) {
        if (event instanceof CustomerCreatedEvent createdEvent) {
            handleCustomerCreated(createdEvent);
        } else {
            log.error("Unsupported customer event type: {}", event.getClass().getName());
        }
    }

    private void handleCustomerCreated(CustomerCreatedEvent event) {
        var customerId = event.getCustomer().getCustomerId().id();
        var customerEventAvroModel = CustomerEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setCustomerId(customerId)
                .setEmail(event.getCustomer().getEmail())
                .setType(CustomerEventType.CUSTOMER_CREATED)
                .setCreatedAt(event.getCreatedAt())
                .build();

        outboxService.save(MESSAGE_TYPE_CUSTOMER_CREATED, customerId.toString(), customerEventAvroModel);
        log.info("Outbox entry saved for customer created event: {}", customerId);
    }
}