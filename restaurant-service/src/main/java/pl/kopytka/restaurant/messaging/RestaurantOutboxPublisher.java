package pl.kopytka.restaurant.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.restaurant.RestaurantOrderEventAvroModel;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
class RestaurantOutboxPublisher extends OutboxPublisher {

    private final TopicsConfigData topicsConfigData;
    private final KafkaProducer<String, RestaurantOrderEventAvroModel> restaurantOrderKafkaProducer;

    @Override
    protected void publish(OutboxEntry entry) {
        String messageType = entry.getMessageType();

        if (RestaurantOutboxWriter.MESSAGE_TYPE_RESTAURANT_ORDER_APPROVED.equals(messageType) ||
                RestaurantOutboxWriter.MESSAGE_TYPE_RESTAURANT_ORDER_REJECTED.equals(messageType)) {
            RestaurantOrderEventAvroModel avro = deserializeAvroPayload(entry, RestaurantOrderEventAvroModel.class);
            publishRestaurantOrderEvent(avro, entry.getMessageKey());
        } else {
            log.warn("Unknown message type in outbox entry: {}", messageType);
        }
    }

    void publishRestaurantOrderEvent(RestaurantOrderEventAvroModel avro, String messageKey) {
        restaurantOrderKafkaProducer.send(
                topicsConfigData.getRestaurantOrderEvent(),
                messageKey,
                avro
        );
    }
}
