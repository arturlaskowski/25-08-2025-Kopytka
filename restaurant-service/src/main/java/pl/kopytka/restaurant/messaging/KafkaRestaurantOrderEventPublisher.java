package pl.kopytka.restaurant.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kopytka.avro.restaurant.RestaurantEventType;
import pl.kopytka.avro.restaurant.RestaurantOrderApprovedAvroEvent;
import pl.kopytka.avro.restaurant.RestaurantOrderEventAvroModel;
import pl.kopytka.avro.restaurant.RestaurantOrderRejectedAvroEvent;
import pl.kopytka.restaurant.domain.event.OrderApprovedEvent;
import pl.kopytka.restaurant.domain.event.OrderRejectedEvent;
import pl.kopytka.restaurant.domain.event.RestaurantOrderEvent;
import pl.kopytka.restaurant.messaging.exception.RestaurantMessagingException;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class KafkaRestaurantOrderEventPublisher {

    private final KafkaTemplate<String, RestaurantOrderEventAvroModel> kafkaTemplate;
    private final TopicsConfigData topicsConfigData;

    @TransactionalEventListener
    @Async
    public void handleEventAfterCommitAndSendToKafka(RestaurantOrderEvent event) {
        try {
            switch (event) {
                case OrderApprovedEvent orderApprovedEvent -> publishApprovalEvent(orderApprovedEvent);
                case OrderRejectedEvent orderRejectedEvent -> publishRejectionEvent(orderRejectedEvent);
                default -> {
                    log.error("Cannot publish unknown event type: {}", event.getClass().getName());
                    throw new RestaurantMessagingException("Cannot publish unknown event type: " + event.getClass().getName());
                }
            }
        } catch (Exception e) {
            log.error("Error while publishing event to Kafka", e);
            throw new RestaurantMessagingException("Error while publishing event to Kafka", e);
        }
    }

    private void publishApprovalEvent(OrderApprovedEvent event) {
        var approvedEvent = RestaurantOrderApprovedAvroEvent.newBuilder()
                .setRestaurantId(event.getRestaurantId())
                .setOrderId(event.getOrderId())
                .setCreatedAt(Instant.now())
                .build();

        var eventMessage = RestaurantOrderEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(RestaurantEventType.ORDER_APPROVED)
                .setPayload(approvedEvent)
                .build();

        kafkaTemplate.send(topicsConfigData.getRestaurantOrderEvent(), event.getOrderId().toString(), eventMessage);
        log.info("Restaurant approval event sent to kafka for order id: {}", event.getOrderId());
    }

    private void publishRejectionEvent(OrderRejectedEvent event) {
        var rejectedEvent = RestaurantOrderRejectedAvroEvent.newBuilder()
                .setRestaurantId(event.getRestaurantId())
                .setOrderId(event.getOrderId())
                .setCreatedAt(Instant.now())
                .setFailureMessages(event.getRejectionReason())
                .build();

        var eventMessage = RestaurantOrderEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(RestaurantEventType.ORDER_REJECTED)
                .setPayload(rejectedEvent)
                .build();

        kafkaTemplate.send(topicsConfigData.getRestaurantOrderEvent(), event.getOrderId().toString(), eventMessage);
        log.info("Restaurant rejection event sent to kafka for order id: {}", event.getOrderId());
    }
}
