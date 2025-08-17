package pl.kopytka.customer.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.customer.domain.event.CustomerCreatedEvent;

@Component
@RequiredArgsConstructor
public class CustomerCreatedEventPublisher implements DomainEventPublisher<CustomerCreatedEvent> {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, CustomerEventAvroModel> kafkaProducer;

    @Override
    public void publish(CustomerCreatedEvent domainEvent) {
        var customerId = domainEvent.getCustomer().getCustomerId().id();
        var customerEventAvroModel = new CustomerEventAvroModel(customerId,
                CustomerEventType.CUSTOMER_CREATED, domainEvent.getCreatedAt());

        kafkaProducer.send(topicsConfigData.getCustomerEvent(),
                customerId.toString(),
                customerEventAvroModel);
    }
}