package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;
import pl.kopytka.order.application.replicaiton.CustomerView;
import pl.kopytka.order.application.replicaiton.CustomerViewService;

import java.util.List;

@Component
@RequiredArgsConstructor
class CustomerEventListener extends AbstractKafkaConsumer<CustomerEventAvroModel> {

    private final CustomerViewService customerViewService;

    @Override
    @KafkaListener(id = "CustomerEventListener",
            groupId = "${order-service.kafka.group-id}",
            topics = "${order-service.kafka.topics.customer-event}")
    public void receive(@Payload List<CustomerEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(CustomerEventAvroModel event) {
        if (event.getType().equals(CustomerEventType.CUSTOMER_CREATED)) {
            customerViewService.onCreateCustomer(new CustomerView(event.getCustomerId()));
        }
    }

    @Override
    protected String getMessageTypeName() {
        return "customerEvent";
    }
}