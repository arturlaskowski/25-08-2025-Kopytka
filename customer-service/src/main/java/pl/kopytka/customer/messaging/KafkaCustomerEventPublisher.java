package pl.kopytka.customer.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.customer.domain.event.CustomerEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class KafkaCustomerEventPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, CustomerEventAvroModel> kafkaProducer;

    @TransactionalEventListener
    @Async
    public void handleEventAfterCommitAndSendToKafka(CustomerEvent customerEvent) {
        var customerId = customerEvent.getCustomer().getCustomerId().id();
        var customerEventAvroModel = new CustomerEventAvroModel(UUID.randomUUID(),
                customerId, customerEvent.getCustomer().getEmail(),
                CustomerEventType.CUSTOMER_CREATED, customerEvent.getCreatedAt());

        kafkaProducer.send(topicsConfigData.getCustomerEvent(),
                customerId.toString(),
                customerEventAvroModel);
    }
}
