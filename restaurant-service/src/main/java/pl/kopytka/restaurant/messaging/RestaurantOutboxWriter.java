package pl.kopytka.restaurant.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.avro.restaurant.RestaurantEventType;
import pl.kopytka.avro.restaurant.RestaurantOrderApprovedAvroEvent;
import pl.kopytka.avro.restaurant.RestaurantOrderEventAvroModel;
import pl.kopytka.avro.restaurant.RestaurantOrderRejectedAvroEvent;
import pl.kopytka.common.outbox.OutboxService;
import pl.kopytka.restaurant.domain.RestaurantOrderEventPublisher;
import pl.kopytka.restaurant.domain.event.OrderApprovedEvent;
import pl.kopytka.restaurant.domain.event.OrderRejectedEvent;
import pl.kopytka.restaurant.domain.event.RestaurantOrderEvent;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class RestaurantOutboxWriter implements RestaurantOrderEventPublisher {

    static final String MESSAGE_TYPE_RESTAURANT_ORDER_APPROVED = "restaurantOrderApproved";
    static final String MESSAGE_TYPE_RESTAURANT_ORDER_REJECTED = "restaurantOrderRejected";

    private final OutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(RestaurantOrderEvent event) {
        switch (event) {
            case OrderApprovedEvent approvedEvent -> handleOrderApproved(approvedEvent);
            case OrderRejectedEvent rejectedEvent -> handleOrderRejected(rejectedEvent);
            default ->
                    throw new IllegalStateException("Unsupported restaurant order event type:" + event.getClass().getName());
        }
    }

    private void handleOrderApproved(OrderApprovedEvent event) {
        RestaurantOrderApprovedAvroEvent approvedEvent = RestaurantOrderApprovedAvroEvent.newBuilder()
                .setRestaurantId(event.getRestaurantId())
                .setOrderId(event.getOrderId())
                .setCreatedAt(Instant.now())
                .build();

        RestaurantOrderEventAvroModel eventMessage = RestaurantOrderEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(RestaurantEventType.ORDER_APPROVED)
                .setPayload(approvedEvent)
                .build();

        outboxService.save(MESSAGE_TYPE_RESTAURANT_ORDER_APPROVED, event.getOrderId().toString(), eventMessage);
        log.info("Outbox entry saved for order approved event - order: {}", event.getOrderId());
    }

    private void handleOrderRejected(OrderRejectedEvent event) {
        RestaurantOrderRejectedAvroEvent rejectedEvent = RestaurantOrderRejectedAvroEvent.newBuilder()
                .setRestaurantId(event.getRestaurantId())
                .setOrderId(event.getOrderId())
                .setCreatedAt(Instant.now())
                .setFailureMessages(event.getRejectionReason())
                .build();

        RestaurantOrderEventAvroModel eventMessage = RestaurantOrderEventAvroModel.newBuilder()
                .setMessageId(UUID.randomUUID())
                .setType(RestaurantEventType.ORDER_REJECTED)
                .setPayload(rejectedEvent)
                .build();

        outboxService.save(MESSAGE_TYPE_RESTAURANT_ORDER_REJECTED, event.getOrderId().toString(), eventMessage);
        log.info("Outbox entry saved for order rejected event - order: {}", event.getOrderId());
    }
}
