package pl.kopytka.order.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.restaurant.RestaurantOrderApprovedAvroEvent;
import pl.kopytka.avro.restaurant.RestaurantOrderEventAvroModel;
import pl.kopytka.avro.restaurant.RestaurantOrderRejectedAvroEvent;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;
import pl.kopytka.order.application.OrderApplicationService;

import java.util.List;

@Component
@RequiredArgsConstructor
class RestaurantEventListener extends AbstractKafkaConsumer<RestaurantOrderEventAvroModel> {

    private final OrderApplicationService orderApplicationService;

    @Override
    @KafkaListener(id = "RestaurantEventListener",
            groupId = "${order-service.kafka.group-id}",
            topics = "${order-service.kafka.topics.restaurant-order-event}")
    public void receive(@Payload List<RestaurantOrderEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(RestaurantOrderEventAvroModel event) {
        switch (event.getType()) {
            case ORDER_APPROVED ->
                    handleRestaurantOrderApproved(((RestaurantOrderApprovedAvroEvent) event.getPayload()));
            case ORDER_REJECTED ->
                    handleRestaurantOrderRejected((RestaurantOrderRejectedAvroEvent) event.getPayload());
        }
    }

    private void handleRestaurantOrderApproved(RestaurantOrderApprovedAvroEvent event) {
        var orderId = new OrderId(event.getOrderId());
        orderApplicationService.approveOrder(orderId);
    }

    private void handleRestaurantOrderRejected(RestaurantOrderRejectedAvroEvent event) {
        var orderId = new OrderId(event.getOrderId());
        orderApplicationService.initCancelOrder(orderId, event.getFailureMessages());
    }

    @Override
    protected String getMessageTypeName() {
        return "restaurantEvent";
    }
}