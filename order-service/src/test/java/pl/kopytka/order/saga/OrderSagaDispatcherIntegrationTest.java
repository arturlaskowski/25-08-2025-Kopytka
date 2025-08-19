package pl.kopytka.order.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.avro.payment.PaymentCommandAvroModel;
import pl.kopytka.avro.restaurant.RestaurantOrderCommandAvroModel;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.OutboxIntegrationTest;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.ProductId;
import pl.kopytka.common.domain.valueobject.Quantity;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.common.outbox.OutboxEntry;
import pl.kopytka.common.outbox.OutboxStatus;
import pl.kopytka.common.saga.SagaStatus;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;
import pl.kopytka.order.domain.event.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AcceptanceTest
@Transactional
class OrderSagaDispatcherIntegrationTest extends OutboxIntegrationTest {

    @Autowired
    private OrderSagaDispatcher sagaDispatcher;

    @Autowired
    private OrderSagaRepository sagaRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing sagas and outbox entries before each test
        sagaRepository.deleteAll();
        clearOutboxEntries();
    }

    @Test
    @DisplayName("Should create saga and save payment command to outbox when order is created")
    void shouldCreateSagaAndSavePaymentCommandWhenOrderCreated() {
        // given
        Order order = createValidOrder();
        OrderCreatedEvent event = new OrderCreatedEvent(order);

        // when
        sagaDispatcher.publish(event);

        // then
        // Verify saga was created and saved
        Iterable<OrderSaga> sagasIterable = sagaRepository.findAll();
        List<OrderSaga> sagas = new java.util.ArrayList<>();
        sagasIterable.forEach(sagas::add);
        assertThat(sagas).hasSize(1);
        
        OrderSaga saga = sagas.getFirst();
        assertThat(saga.getOrderId()).isEqualTo(order.getId().id());
        assertThat(saga.getCustomerId()).isEqualTo(order.getCustomerId().id());
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        assertThat(saga.getCreatedAt()).isNotNull();
        assertThat(saga.getUpdatedAt()).isNotNull();
        assertThat(saga.getErrorMessage()).isNull();

        // Verify payment command was saved to outbox
        List<OutboxEntry> outboxEntries = findOutboxEntriesByMessageType(PaymentCommandAvroModel.class.getTypeName());
        assertThat(outboxEntries).hasSize(1);
        
        OutboxEntry paymentCommand = outboxEntries.getFirst();
        assertThat(paymentCommand.getMessageType()).isEqualTo(PaymentCommandAvroModel.class.getTypeName());
        assertThat(paymentCommand.getMessageKey()).isEqualTo(order.getId().id().toString());
        assertThat(paymentCommand.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(paymentCommand.getPayload()).isNotNull();
    }

    @Test
    @DisplayName("Should update saga status and save restaurant command when order is paid")
    void shouldUpdateSagaStatusAndSaveRestaurantCommandWhenOrderPaid() {
        // given
        Order order = createValidOrder();
        order.pay(); // Transition to PAID status
        
        // Create saga first (simulate that order was already created)
        OrderSaga saga = OrderSaga.create(order.getId().id(), order.getCustomerId().id());
        sagaRepository.save(saga);
        
        OrderPaidEvent event = new OrderPaidEvent(order);

        // when
        sagaDispatcher.publish(event);

        // then
        // Verify saga status was updated
        OrderSaga updatedSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(updatedSaga.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        assertThat(updatedSaga.getUpdatedAt()).isAfter(saga.getUpdatedAt());

        // Verify restaurant command was saved to outbox
        List<OutboxEntry> outboxEntries = findOutboxEntriesByMessageType(RestaurantOrderCommandAvroModel.class.getTypeName());
        assertThat(outboxEntries).hasSize(1);
        
        OutboxEntry restaurantCommand = outboxEntries.getFirst();
        assertThat(restaurantCommand.getMessageType()).isEqualTo(RestaurantOrderCommandAvroModel.class.getTypeName());
        assertThat(restaurantCommand.getMessageKey()).isEqualTo(order.getId().id().toString());
        assertThat(restaurantCommand.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(restaurantCommand.getPayload()).isNotNull();
    }

    @Test
    @DisplayName("Should complete saga when order is approved")
    void shouldCompleteSagaWhenOrderApproved() {
        // given
        Order order = createValidOrder();
        order.pay();
        order.approve(); // Transition to APPROVED status
        
        // Create saga first
        OrderSaga saga = OrderSaga.create(order.getId().id(), order.getCustomerId().id());
        sagaRepository.save(saga);
        
        OrderApprovedEvent event = new OrderApprovedEvent(order);

        // when
        sagaDispatcher.publish(event);

        // then
        // Verify saga was completed
        OrderSaga updatedSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(updatedSaga.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
        assertThat(updatedSaga.getUpdatedAt()).isAfter(saga.getUpdatedAt());
        
        // No new outbox entries should be created for completion
        List<OutboxEntry> outboxEntries = getAllOutboxEntries();
        assertThat(outboxEntries).isEmpty();
    }

    @Test
    @DisplayName("Should compensate saga when order is canceled")
    void shouldCompensateSagaWhenOrderCanceled() {
        // given
        Order order = createValidOrder();
        String failureMessage = "Payment failed - insufficient funds";
        order.cancel(failureMessage); // Transition to CANCELLED status
        
        // Create saga first
        OrderSaga saga = OrderSaga.create(order.getId().id(), order.getCustomerId().id());
        sagaRepository.save(saga);
        
        OrderCanceledEvent event = new OrderCanceledEvent(order);

        // when
        sagaDispatcher.publish(event);

        // then
        // Verify saga was compensated
        OrderSaga updatedSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(updatedSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(updatedSaga.getErrorMessage()).isEqualTo(failureMessage);
        assertThat(updatedSaga.getUpdatedAt()).isAfter(saga.getUpdatedAt());
        
        // No new outbox entries should be created for compensation
        List<OutboxEntry> outboxEntries = getAllOutboxEntries();
        assertThat(outboxEntries).isEmpty();
    }

    @Test
    @DisplayName("Should start compensation process when order cancel is initiated")
    void shouldStartCompensationProcessWhenOrderCancelInitiated() {
        // given
        Order order = createValidOrder();
        order.pay();
        String failureMessage = "Restaurant rejected order - ingredients not available";
        order.initCancel(failureMessage); // Transition to CANCELLING status
        
        // Create saga first
        OrderSaga saga = OrderSaga.create(order.getId().id(), order.getCustomerId().id());
        sagaRepository.save(saga);
        
        OrderCancelInitiatedEvent event = new OrderCancelInitiatedEvent(order);

        // when
        sagaDispatcher.publish(event);

        // then
        // Verify saga status was updated to compensating
        OrderSaga updatedSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(updatedSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(updatedSaga.getErrorMessage()).isEqualTo(failureMessage);
        assertThat(updatedSaga.getUpdatedAt()).isAfter(saga.getUpdatedAt());

        // Verify cancel payment command was saved to outbox
        List<OutboxEntry> outboxEntries = findOutboxEntriesByMessageType(PaymentCommandAvroModel.class.getTypeName());
        assertThat(outboxEntries).hasSize(1);
        
        OutboxEntry cancelPaymentCommand = outboxEntries.getFirst();
        assertThat(cancelPaymentCommand.getMessageType()).isEqualTo(PaymentCommandAvroModel.class.getTypeName());
        assertThat(cancelPaymentCommand.getMessageKey()).isEqualTo(order.getId().id().toString());
        assertThat(cancelPaymentCommand.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(cancelPaymentCommand.getPayload()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when saga not found for paid event")
    void shouldThrowExceptionWhenSagaNotFoundForPaidEvent() {
        // given
        Order order = createValidOrder();
        order.pay();
        OrderPaidEvent event = new OrderPaidEvent(order);

        // when & then
        assertThatThrownBy(() -> sagaDispatcher.publish(event))
                .isInstanceOf(RuntimeException.class); // NoSuchElementException wrapped in RuntimeException
    }

    @Test
    @DisplayName("Should throw exception when saga not found for approved event")
    void shouldThrowExceptionWhenSagaNotFoundForApprovedEvent() {
        // given
        Order order = createValidOrder();
        order.pay();
        order.approve();
        OrderApprovedEvent event = new OrderApprovedEvent(order);

        // when & then
        assertThatThrownBy(() -> sagaDispatcher.publish(event))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when saga not found for canceled event")
    void shouldThrowExceptionWhenSagaNotFoundForCanceledEvent() {
        // given
        Order order = createValidOrder();
        order.cancel("Payment failed");
        OrderCanceledEvent event = new OrderCanceledEvent(order);

        // when & then
        assertThatThrownBy(() -> sagaDispatcher.publish(event))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when saga not found for cancel initiated event")
    void shouldThrowExceptionWhenSagaNotFoundForCancelInitiatedEvent() {
        // given
        Order order = createValidOrder();
        order.pay();
        order.initCancel("Restaurant rejected");
        OrderCancelInitiatedEvent event = new OrderCancelInitiatedEvent(order);

        // when & then
        assertThatThrownBy(() -> sagaDispatcher.publish(event))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception for unsupported event type")
    void shouldThrowExceptionForUnsupportedEventType() {
        // given
        Order order = createValidOrder();
        OrderEvent unsupportedEvent = new UnsupportedOrderEvent(order);

        // when & then
        assertThatThrownBy(() -> sagaDispatcher.publish(unsupportedEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported event type: UnsupportedOrderEvent");
    }

    @Test
    @DisplayName("Should handle multiple saga operations in sequence")
    void shouldHandleMultipleSagaOperationsInSequence() {
        // given
        Order order = createValidOrder();
        
        // when - simulate full saga flow
        // 1. Order created
        sagaDispatcher.publish(new OrderCreatedEvent(order));
        
        // 2. Order paid
        order.pay();
        sagaDispatcher.publish(new OrderPaidEvent(order));
        
        // 3. Order approved
        order.approve();
        sagaDispatcher.publish(new OrderApprovedEvent(order));

        // then
        // Verify final saga state
        OrderSaga finalSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(finalSaga.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
        assertThat(finalSaga.getErrorMessage()).isNull();
        
        // Verify outbox entries - should have payment command and restaurant command
        List<OutboxEntry> paymentCommands = findOutboxEntriesByMessageType(PaymentCommandAvroModel.class.getTypeName());
        List<OutboxEntry> restaurantCommands = findOutboxEntriesByMessageType(RestaurantOrderCommandAvroModel.class.getTypeName());
        
        assertThat(paymentCommands).hasSize(1);
        assertThat(restaurantCommands).hasSize(1);
    }

    @Test
    @DisplayName("Should handle compensation flow properly")
    void shouldHandleCompensationFlowProperly() {
        // given
        Order order = createValidOrder();
        String failureMessage = "Restaurant rejected order";
        
        // when - simulate compensation flow
        // 1. Order created
        sagaDispatcher.publish(new OrderCreatedEvent(order));
        
        // 2. Order paid
        order.pay();
        sagaDispatcher.publish(new OrderPaidEvent(order));
        
        // 3. Order cancellation initiated (restaurant rejection)
        order.initCancel(failureMessage);
        sagaDispatcher.publish(new OrderCancelInitiatedEvent(order));
        
        // 4. Order canceled (payment compensated)
        order.cancel(failureMessage);
        sagaDispatcher.publish(new OrderCanceledEvent(order));

        // then
        // Verify final saga state
        OrderSaga finalSaga = sagaRepository.findByOrderId(order.getId().id()).orElseThrow();
        assertThat(finalSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(finalSaga.getErrorMessage()).contains(failureMessage);
        
        // Verify outbox entries - should have create payment, restaurant approve, and cancel payment commands
        List<OutboxEntry> paymentCommands = findOutboxEntriesByMessageType(PaymentCommandAvroModel.class.getTypeName());
        List<OutboxEntry> restaurantCommands = findOutboxEntriesByMessageType(RestaurantOrderCommandAvroModel.class.getTypeName());
        
        assertThat(paymentCommands).hasSize(2); // Create payment + Cancel payment
        assertThat(restaurantCommands).hasSize(1); // Restaurant approve command
    }

    private Order createValidOrder() {
        CustomerId customerId = CustomerId.newOne();
        RestaurantId restaurantId = RestaurantId.newOne();
        OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");
        
        Set<OrderItem> basketItems = createValidBasketItems();
        Money totalPrice = new Money(BigDecimal.valueOf(39.99));
        
        return new Order(customerId, restaurantId, deliveryAddress, totalPrice, basketItems);
    }

    private Set<OrderItem> createValidBasketItems() {
        Set<OrderItem> basketItems = new HashSet<>();
        
        ProductId productId1 = new ProductId(UUID.randomUUID());
        Money price1 = new Money(BigDecimal.valueOf(19.99));
        Quantity quantity1 = new Quantity(1);
        
        ProductId productId2 = new ProductId(UUID.randomUUID());
        Money price2 = new Money(BigDecimal.valueOf(20.00));
        Quantity quantity2 = new Quantity(1);
        
        basketItems.add(new OrderItem(productId1, price1, quantity1, 1));
        basketItems.add(new OrderItem(productId2, price2, quantity2, 2));
        
        return basketItems;
    }

    // Custom unsupported event for testing
    private static class UnsupportedOrderEvent extends OrderEvent {
        public UnsupportedOrderEvent(Order order) {
            super(order, java.time.Instant.now());
        }
    }
}
