package pl.kopytka.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.kopytka.common.domain.valueobject.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    // Test data constants
    private static final BigDecimal VALID_TOTAL_PRICE = new BigDecimal("65.50");
    private static final String EXPECTED_PRICE_ERROR_MESSAGE = "Order price: 0.00 must be greater than zero";
    private static final String EXPECTED_TOTAL_PRICE_MISMATCH_ERROR = "Total order price: 100.00 is different than basket items total: 65.50";
    private static final String EXPECTED_INCORRECT_STATE_PAYMENT_ERROR = "The payment operation cannot be performed. Order is in incorrect state";
    private static final String EXPECTED_INCORRECT_STATE_APPROVAL_ERROR = "The approve operation cannot be performed. Order is in incorrect state";

    @Nested
    @DisplayName("Order Creation Tests")
    class OrderCreationTests {
        @Test
        @DisplayName("Should create order with valid details")
        void shouldCreateOrderWithValidDetails() {
            //given
            CustomerId customerId = CustomerId.newOne();
            RestaurantId restaurantId = RestaurantId.newOne();
            OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");

            Set<OrderItem> basketItems = createValidBasketItems();
            Money totalPrice = new Money(VALID_TOTAL_PRICE);

            Instant beforeCreation = Instant.now();

            //when
            Order order = new Order(customerId, restaurantId, deliveryAddress, totalPrice, basketItems);

            Instant afterCreation = Instant.now();

            //then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getDeliveryAddress()).isEqualTo(deliveryAddress);
            assertThat(order.getPrice()).isEqualTo(totalPrice);
            assertThat(order.getBasket()).containsExactlyInAnyOrderElementsOf(basketItems);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.isPendingStatus()).isTrue();
            assertThat(order.getCreationDate())
                    .isAfterOrEqualTo(beforeCreation)
                    .isBeforeOrEqualTo(afterCreation);
        }

        @Test
        void shouldThrowExceptionWhenPriceIsNotPositive() {
            //given
            CustomerId customerId = CustomerId.newOne();
            RestaurantId restaurantId = RestaurantId.newOne();
            OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");

            Set<OrderItem> basketItems = createValidBasketItems();
            Money zeroPrice = new Money(BigDecimal.ZERO);

            //expected
            assertThatThrownBy(() -> new Order(customerId, restaurantId, deliveryAddress, zeroPrice, basketItems))
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_PRICE_ERROR_MESSAGE);
        }

        @Test
        void shouldThrowExceptionWhenTotalPriceDoesNotMatchBasketItemsTotal() {
            //given
            CustomerId customerId = CustomerId.newOne();
            RestaurantId restaurantId = RestaurantId.newOne();
            OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");

            Set<OrderItem> basketItems = createValidBasketItems();
            Money incorrectTotalPrice = new Money(new BigDecimal("100.00")); // Correct price is 65.50

            //expected
            assertThatThrownBy(() -> new Order(customerId, restaurantId, deliveryAddress, incorrectTotalPrice, basketItems))
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_TOTAL_PRICE_MISMATCH_ERROR);
        }

        @Test
        void shouldThrowExceptionWhenBasketItemHasInvalidPrice() {
            //expected
            assertThatThrownBy(() -> {
                // First item - valid
                RestaurantId restaurantId = RestaurantId.newOne();
                ProductId productId1 = new ProductId(UUID.randomUUID());
                Money price1 = new Money(new BigDecimal("15.00"));
                Quantity quantity1 = new Quantity(2);
                Money totalPrice1 = new Money(new BigDecimal("30.00"));

                // Second item - invalid (total price should be 15.00 but is set to 20.00)
                ProductId productId2 = new ProductId(UUID.randomUUID());
                Money price2 = new Money(new BigDecimal("5.00"));
                Quantity quantity2 = new Quantity(3);
                Money incorrectTotalPrice = new Money(new BigDecimal("20.00")); // Correct would be 15.00

                Set<OrderItem> basketItems = new HashSet<>();
                basketItems.add(new OrderItem(productId1, price1, quantity1, totalPrice1, 1));
                basketItems.add(new OrderItem(productId2, price2, quantity2, incorrectTotalPrice, 2));

                CustomerId customerId = new CustomerId(UUID.randomUUID());
                OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");
                Money totalPrice = new Money(new BigDecimal("50.00"));

                new Order(customerId, restaurantId, deliveryAddress, totalPrice, basketItems);
            })
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining("Total price should be equal to price multiplied by quantity");
        }
    }

    @Nested
    @DisplayName("Order Payment Tests")
    class OrderPaymentTests {
        @Test
        @DisplayName("Should pay order successfully")
        void shouldPayOrderSuccessfully() {
            //given
            Order order = createValidOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            //when
            order.pay();

            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.isPaidStatus()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when paying non-pending order")
        void shouldThrowExceptionWhenPayingNonPendingOrder() {
            //given
            Order order = createValidOrder();
            order.pay(); // Change status to PAID

            //expected
            assertThatThrownBy(order::pay)
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_INCORRECT_STATE_PAYMENT_ERROR);
        }

        @Test
        @DisplayName("Should throw exception when paying already approved order")
        void shouldThrowExceptionWhenPayingApprovedOrder() {
            //given
            Order order = createValidOrder();
            order.pay(); // Change status to PAID
            order.approve(); // Change status to APPROVED

            //expected
            assertThatThrownBy(order::pay)
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_INCORRECT_STATE_PAYMENT_ERROR);
        }
    }

    @Nested
    @DisplayName("Order Approval Tests")
    class OrderApprovalTests {
        @Test
        @DisplayName("Should approve order successfully")
        void shouldApproveOrderSuccessfully() {
            //given
            Order order = createValidOrder();
            order.pay(); // Change status to PAID

            //when
            order.approve();

            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
            assertThat(order.isApprovedStatus()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when approving non-paid order")
        void shouldThrowExceptionWhenApprovingNonPaidOrder() {
            //given
            Order order = createValidOrder();
            // Status is PENDING, not PAID

            //expected
            assertThatThrownBy(order::approve)
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_INCORRECT_STATE_APPROVAL_ERROR);
        }

        @Test
        @DisplayName("Should throw exception when approving already approved order")
        void shouldThrowExceptionWhenApprovingAlreadyApprovedOrder() {
            //given
            Order order = createValidOrder();
            order.pay(); // Change status to PAID
            order.approve(); // Change status to APPROVED

            //expected
            assertThatThrownBy(order::approve)
                    .isInstanceOf(OrderDomainException.class)
                    .hasMessageContaining(EXPECTED_INCORRECT_STATE_APPROVAL_ERROR);
        }
    }

    @Nested
    @DisplayName("Order Status Transition Tests")
    class OrderStatusTransitionTests {
        @Test
        @DisplayName("Should transition from PENDING to PAID to APPROVED correctly")
        void shouldTransitionFromPendingToPaidToApprovedCorrectly() {
            //given
            Order order = createValidOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.isPendingStatus()).isTrue();
            assertThat(order.isPaidStatus()).isFalse();
            assertThat(order.isApprovedStatus()).isFalse();

            //when - transition to PAID
            order.pay();

            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.isPendingStatus()).isFalse();
            assertThat(order.isPaidStatus()).isTrue();
            assertThat(order.isApprovedStatus()).isFalse();

            //when - transition to APPROVED
            order.approve();

            //then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
            assertThat(order.isPendingStatus()).isFalse();
            assertThat(order.isPaidStatus()).isFalse();
            assertThat(order.isApprovedStatus()).isTrue();
        }
    }

    private Set<OrderItem> createValidBasketItems() {
        Set<OrderItem> basketItems = new HashSet<>();

        // First item
        ProductId productId1 = new ProductId(UUID.randomUUID());
        Money price1 = new Money(new BigDecimal("15.50"));
        Quantity quantity1 = new Quantity(3);
        Money totalPrice1 = new Money(new BigDecimal("46.50"));

        // Second item
        ProductId productId2 = new ProductId(UUID.randomUUID());
        Money price2 = new Money(new BigDecimal("19.00"));
        Quantity quantity2 = new Quantity(1);
        Money totalPrice2 = new Money(new BigDecimal("19.00"));

        basketItems.add(new OrderItem(productId1, price1, quantity1, totalPrice1, 1));
        basketItems.add(new OrderItem(productId2, price2, quantity2, totalPrice2, 2));

        return basketItems;
    }

    private Order createValidOrder() {
        RestaurantId restaurantId = RestaurantId.newOne();
        CustomerId customerId = CustomerId.newOne();
        OrderAddress deliveryAddress = new OrderAddress("Main Street", "12-345", "New York", "10A");

        Set<OrderItem> basketItems = createValidBasketItems();
        Money totalPrice = new Money(VALID_TOTAL_PRICE);

        return new Order(customerId, restaurantId, deliveryAddress, totalPrice, basketItems);
    }
}
