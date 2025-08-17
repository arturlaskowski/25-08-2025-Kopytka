package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.ProcessPaymentCommandAvroModel;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.order.application.ProcessPaymentCommandPublisher;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class KafkaPaymentCommandPublisher implements ProcessPaymentCommandPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, ProcessPaymentCommandAvroModel> kafkaProducer;

    @Override
    public void publishProcessPaymentCommand(OrderId orderId, CustomerId customerId, Money price) {
        var processPaymentCommandAvroModel = new ProcessPaymentCommandAvroModel(
                orderId.id(),
                customerId.id(),
                price.amount(),
                Instant.now()
        );

        kafkaProducer.send(topicsConfigData.getPaymentCommand(),
                orderId.id().toString(),
                processPaymentCommandAvroModel
        );
    }
}
