package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.PaymentCommandAvroModel;
import pl.kopytka.avro.restaurant.RestaurantOrderCommandAvroModel;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
class OrderOutboxPublisher extends OutboxPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, PaymentCommandAvroModel> paymentCommandProducer;
    private final KafkaProducer<String, RestaurantOrderCommandAvroModel> restaurantCommandProducer;

    @Override
    protected void publish(OutboxEntry entry) {
        String messageType = entry.getMessageType();

        if (PaymentCommandAvroModel.class.getTypeName().equals(messageType)) {
            PaymentCommandAvroModel payment = deserializeAvroPayload(entry, PaymentCommandAvroModel.class);
            publishPaymentCommand(payment, entry.getMessageKey());
        } else if (RestaurantOrderCommandAvroModel.class.getTypeName().equals(messageType)) {
            RestaurantOrderCommandAvroModel restaurant = deserializeAvroPayload(entry, RestaurantOrderCommandAvroModel.class);
            publishRestaurantCommand(restaurant, entry.getMessageKey());
        } else {
            log.warn("Unknown message type in outbox entry: {}", messageType);
        }
    }

    void publishPaymentCommand(PaymentCommandAvroModel avro, String messageKey) {
        paymentCommandProducer.send(
                topicsConfigData.getPaymentCommand(),
                messageKey,
                avro
        );
    }

    void publishRestaurantCommand(RestaurantOrderCommandAvroModel avro, String messageKey) {
        restaurantCommandProducer.send(
                topicsConfigData.getRestaurantOrderCommand(),
                messageKey,
                avro
        );
    }
}
