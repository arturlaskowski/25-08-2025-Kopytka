package pl.kopytka.customer.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.customer.domain.CustomerEventPublisher;
import pl.kopytka.customer.domain.event.CustomerEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class KafkaCustomerEventPublisher implements CustomerEventPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, CustomerEventAvroModel> kafkaProducer;

    @Override
    public void publish(CustomerEvent customerEvent) {
        var customerId = customerEvent.getCustomer().getCustomerId().id();
        var customerEventAvroModel = new CustomerEventAvroModel(UUID.randomUUID(), customerId,
                CustomerEventType.CUSTOMER_CREATED, customerEvent.getCreatedAt());

        kafkaProducer.send(topicsConfigData.getCustomerEvent(),
                customerId.toString(),
                customerEventAvroModel);
    }
}
