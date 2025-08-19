package pl.kopytka.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.avro.payment.CancelPaymentAvroCommand;
import pl.kopytka.avro.payment.CreatePaymentAvroCommand;
import pl.kopytka.avro.payment.PaymentCommandAvroModel;
import pl.kopytka.avro.payment.PaymentCommandType;
import pl.kopytka.avro.restaurant.Product;
import pl.kopytka.avro.restaurant.RestaurantApproveOrderAvroCommand;
import pl.kopytka.avro.restaurant.RestaurantCommandType;
import pl.kopytka.avro.restaurant.RestaurantOrderCommandAvroModel;
import pl.kopytka.common.outbox.OutboxService;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderEventPublisher;
import pl.kopytka.order.domain.event.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaDispatcher implements OrderEventPublisher {

    private final OrderSagaRepository orderSagaRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(OrderEvent event) {
        switch (event) {
            case OrderCreatedEvent createdEvent -> handle(createdEvent);
            case OrderPaidEvent paidEvent -> handle(paidEvent);
            case OrderCanceledEvent canceledEvent -> handle(canceledEvent);
            case OrderApprovedEvent approvedEvent -> handle(approvedEvent);
            case OrderCancelInitiatedEvent cancelInitiatedEvent -> handle(cancelInitiatedEvent);
            default ->
                    throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getSimpleName());
        }
    }

    private void handle(OrderCreatedEvent event) {
        var orderId = event.getOrder().getId();
        var customerId = event.getOrder().getCustomerId();

        OrderSaga saga = OrderSaga.create(orderId.id(), customerId.id());
        orderSagaRepository.save(saga);

        var processPaymentCommand = createProcessPaymentCommand(event.getOrder());
        outboxService.save(processPaymentCommand.getClass().getTypeName(),
                orderId.id().toString(),
                processPaymentCommand);
    }

    private void handle(OrderPaidEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.processing();
        orderSagaRepository.save(saga);

        var restaurantApproveCommand = createRestaurantApproveCommand(event.getOrder());
        outboxService.save(restaurantApproveCommand.getClass().getTypeName(),
                orderId.toString(),
                restaurantApproveCommand);
    }

    private void handle(OrderCanceledEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.compensated(event.getOrder().getFailureMessages());
        orderSagaRepository.save(saga);
        //order cancelled
    }

    private void handle(OrderApprovedEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.complete();
        orderSagaRepository.save(saga);
        //order approved
    }

    private void handle(OrderCancelInitiatedEvent event) {
        var orderId = event.getOrder().getId().id();
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId).orElseThrow();

        saga.compensating(event.getOrder().getFailureMessages());
        orderSagaRepository.save(saga);

        var cancelPaymentCommand = createCancelPaymentCommand(event.getOrder());
        outboxService.save(cancelPaymentCommand.getClass().getTypeName(),
                orderId.toString(),
                cancelPaymentCommand);
    }

    private PaymentCommandAvroModel createProcessPaymentCommand(Order order) {
        var orderId = order.getId();
        var customerId = order.getCustomerId();
        var price = order.getPrice();

        var processPaymentCommandAvroModel = new CreatePaymentAvroCommand(
                customerId.id(),
                orderId.id(),
                price.amount(),
                Instant.now()
        );
        return new PaymentCommandAvroModel(
                UUID.randomUUID(), // messageId
                PaymentCommandType.CREATE_PAYMENT,
                processPaymentCommandAvroModel
        );
    }

    private RestaurantOrderCommandAvroModel createRestaurantApproveCommand(Order order) {
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

        return new RestaurantOrderCommandAvroModel(
                UUID.randomUUID(), // messageId
                RestaurantCommandType.APPROVE_ORDER,
                restaurantApproveOrderCommand
        );
    }

    private PaymentCommandAvroModel createCancelPaymentCommand(Order order) {
        var orderId = order.getId();
        var customerId = order.getCustomerId();

        var cancelPaymentCommand = new CancelPaymentAvroCommand(
                orderId.id(),
                customerId.id(),
                Instant.now()
        );

        return new PaymentCommandAvroModel(
                UUID.randomUUID(), // messageId
                PaymentCommandType.CANCEL_PAYMENT,
                cancelPaymentCommand
        );
    }
}