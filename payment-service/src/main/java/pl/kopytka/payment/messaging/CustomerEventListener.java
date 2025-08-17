package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;
import pl.kopytka.payment.application.WalletService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class CustomerEventListener extends AbstractKafkaConsumer<CustomerEventAvroModel> {

    private final WalletService walletService;

    @Override
    @KafkaListener(id = "CustomerEventListener",
            groupId = "${payment-service.kafka.group-id}",
            topics = "${payment-service.kafka.topics.customer-event}")
    public void receive(@Payload List<CustomerEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(CustomerEventAvroModel event) {
        if (event.getType().equals(CustomerEventType.CUSTOMER_CREATED)) {
            walletService.createWallet(new CustomerId(event.getCustomerId()), Money.ZERO);
        }
    }

    @Override
    protected String getMessageTypeName() {
        return "customerEvent";
    }
}