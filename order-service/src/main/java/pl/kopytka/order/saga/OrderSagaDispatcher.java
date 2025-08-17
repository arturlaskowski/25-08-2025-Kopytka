package pl.kopytka.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.avro.payment.CancelPaymentAvroCommand;
import pl.kopytka.avro.payment.CreatePaymentAvroCommand;
import pl.kopytka.avro.payment.PaymentCommandAvroModel;
import pl.kopytka.avro.payment.PaymentCommandType;
import pl.kopytka.avro.restaurant.Product;
import pl.kopytka.avro.restaurant.RestaurantApproveOrderAvroCommand;
import pl.kopytka.avro.restaurant.RestaurantCommandType;
import pl.kopytka.avro.restaurant.RestaurantOrderCommandAvroModel;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.kafka.producer.KafkaProducer;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.event.*;
import pl.kopytka.order.messaging.TopicsConfigData;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaDispatcher {

    private final OrderSagaRepository orderSagaRepository;
    private final TopicsConfigData topics;
    private final KafkaProducer<String, PaymentCommandAvroModel> kafkaPaymentCommandProducer;
    private final KafkaProducer<String, RestaurantOrderCommandAvroModel> kafkaRestaurantCommandProducer;

    @Transactional
    public void handle(OrderCreatedEvent event) {
        var orderId = event.getOrder().getId();
        var customerId = event.getOrder().getCustomerId();
        var amount = event.getOrder().getPrice();

        OrderSaga saga = OrderSaga.create(orderId.id(), customerId.id());
        orderSagaRepository.save(saga);

        sendProcessPaymentCommand(orderId, customerId, amount);
    }

    @Transactional
    public void handle(OrderPaidEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.processing();
        orderSagaRepository.save(saga);

        sendRestaurantApproveCommand(event.getOrder());
    }

    @Transactional
    public void handle(OrderCanceledEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.compensated(event.getOrder().getFailureMessages());
        orderSagaRepository.save(saga);
        //order cancelled
    }

    @Transactional
    public void handle(OrderApprovedEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.complete();
        orderSagaRepository.save(saga);
        //order approved
    }

    @Transactional
    public void handle(OrderCancelInitiatedEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.compensating(event.getOrder().getFailureMessages());
        orderSagaRepository.save(saga);

        sendCancelPaymentCommand(saga);
    }

    private void sendProcessPaymentCommand(OrderId orderId, CustomerId customerId, Money price) {
        var processPaymentCommandAvroModel = new CreatePaymentAvroCommand(
                customerId.id(),
                orderId.id(),
                price.amount(),
                Instant.now()
        );
        var paymentCommandAvroModel = new PaymentCommandAvroModel(
                PaymentCommandType.CREATE_PAYMENT,
                processPaymentCommandAvroModel
        );

        kafkaPaymentCommandProducer.send(topics.getPaymentCommand(), orderId.id().toString(), paymentCommandAvroModel);
    }

    private void sendCancelPaymentCommand(OrderSaga saga) {

        var cancelPaymentCommand = new CancelPaymentAvroCommand(
                saga.getOrderId(),
                saga.getCustomerId(),
                Instant.now()
        );
        var paymentCommandAvroModel = new PaymentCommandAvroModel(
                PaymentCommandType.CANCEL_PAYMENT,
                cancelPaymentCommand
        );

        kafkaPaymentCommandProducer.send(topics.getPaymentCommand(), saga.getOrderId().toString(), paymentCommandAvroModel);
    }

    private void sendRestaurantApproveCommand(Order order) {
        List<Product> products = order.getBasket().stream()
                .map(basketItem -> Product.newBuilder()
                        .setId(basketItem.getProductId().productId())
                        .setQuantity(basketItem.getQuantity().value())
                        .build())
                .toList();

        var restaurantApproveOrderCommand = RestaurantApproveOrderAvroCommand.newBuilder()
                .setRestaurantId(order.getRestaurantId().restaurantId())
                .setOrderId(order.getId().orderId())
                .setProducts(products)
                .setPrice(order.getPrice().amount())
                .setCreatedAt(Instant.now())
                .build();
        var restaurantCommandAvroModel = new RestaurantOrderCommandAvroModel(
                RestaurantCommandType.APPROVE_ORDER,
                restaurantApproveOrderCommand
        );

        kafkaRestaurantCommandProducer.send(topics.getRestaurantOrderCommand(), order.getId().id().toString(), restaurantCommandAvroModel);
    }

}