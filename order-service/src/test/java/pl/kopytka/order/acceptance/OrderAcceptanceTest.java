package pl.kopytka.order.acceptance;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.KafkaIntegrationTest;
import pl.kopytka.common.web.ErrorResponse;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.application.replicaiton.CustomerView;
import pl.kopytka.order.application.replicaiton.CustomerViewService;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.web.dto.BasketItemRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.OrderAddressRequest;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@AcceptanceTest(topics = OrderAcceptanceTest.PAYMENT_COMMAND_TOPIC)
class OrderAcceptanceTest extends KafkaIntegrationTest {

    static final String PAYMENT_COMMAND_TOPIC = "payment-commands";

    @Autowired
    private CustomerViewService customerViewService;

    @BeforeEach
    void setUp() {
        setupKafkaConsumer(PAYMENT_COMMAND_TOPIC);
    }

    @Test
    @DisplayName("""
            given valid order creation request,
            when request is sent,
            then order is created and HTTP 201 status returned with location header""")
    void givenValidOrderCreationRequest_whenRequestIsSent_thenOrderCreatedAndHttp201Returned() {
        // given
        UUID customerId = UUID.randomUUID();
        customerViewService.onCreateCustomer(new CustomerView(customerId));
        UUID productId = UUID.randomUUID();

        OrderAddressRequest address = new OrderAddressRequest(
                "Main Street",
                "12-345",
                "New York",
                "42A"
        );
        BigDecimal totalPrice = BigDecimal.valueOf(39.98);

        List<BasketItemRequest> basketItems = List.of(
                new BasketItemRequest(productId, BigDecimal.valueOf(19.99), 2, totalPrice));


        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                address,
                totalPrice,
                basketItems
        );

        // when
        var postResponse = testRestTemplate.postForEntity(getBaseOrdersUrl(), request, Void.class);

        // then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getHeaders().getLocation()).isNotNull();

        var getResponse = testRestTemplate.getForEntity(postResponse.getHeaders().getLocation(), OrderQuery.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        OrderQuery order = getResponse.getBody();
        assertThat(order).isNotNull();
        assertThat(order.customerId()).isEqualTo(customerId);
        assertThat(order.price()).isEqualByComparingTo(totalPrice);
        assertThat(order.basketItems()).hasSize(1);
        assertThat(order.basketItems().getFirst().productId()).isEqualTo(productId);
        assertThat(order.basketItems().getFirst().price()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(order.basketItems().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            given order exists,
            when payment request is sent,
            then order status is updated to PAID""")
    void givenOrderExists_whenPaymentRequestIsSent_thenOrderStatusIsUpdatedToPaid() throws InterruptedException {
        // given
        UUID orderId = createOrder();

        // when
        var payResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/pay",
                null,
                Void.class
        );

        // then
        assertThat(payResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify the order status was updated
        OrderQuery order = getOrder(orderId);
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);

        // Verify that event was sent to Kafka
        ConsumerRecord<String, String> customerEvent = records.poll(5, TimeUnit.SECONDS);
        assertThat(customerEvent).isNotNull();
        assertThat(customerEvent.topic()).isEqualTo(PAYMENT_COMMAND_TOPIC);
        assertThat(customerEvent.key()).isEqualTo((orderId.toString()));
    }

    @Test
    @DisplayName("""
            given paid order exists,
            when approval request is sent,
            then order status is updated to APPROVED""")
    void givenPaidOrderExists_whenApprovalRequestIsSent_thenOrderStatusIsUpdatedToApproved() {
        // given
        UUID orderId = createOrder();

        // Pay the order first
        var payResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/pay",
                null,
                Void.class
        );
        assertThat(payResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify order is paid
        OrderQuery paidOrder = getOrder(orderId);
        assertThat(paidOrder.status()).isEqualTo(OrderStatus.PAID);

        // when
        var approveResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/approve",
                null,
                Void.class
        );

        // then
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify the order status was updated to APPROVED
        OrderQuery approvedOrder = getOrder(orderId);
        assertThat(approvedOrder.status()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("""
            given pending order exists,
            when approval request is sent,
            then HTTP 400 Bad Request is returned with state error message""")
    void givenPendingOrderExists_whenApprovalRequestIsSent_thenHttp400BadRequestReturned() {
        // given
        UUID orderId = createOrder();

        // Verify order is in PENDING state
        OrderQuery pendingOrder = getOrder(orderId);
        assertThat(pendingOrder.status()).isEqualTo(OrderStatus.PENDING);

        // when
        var approveResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/approve",
                null,
                ErrorResponse.class
        );

        // then
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(approveResponse.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("incorrect state");
    }

    @Test
    @DisplayName("""
            given already approved order,
            when approval request is sent again,
            then HTTP 400 Bad Request is returned with state error message""")
    void givenAlreadyApprovedOrder_whenApprovalRequestIsSentAgain_thenHttp400BadRequestReturned() {
        // given
        UUID orderId = createOrder();

        // Pay the order
        testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/pay",
                null,
                Void.class
        );

        // Approve the order
        testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/approve",
                null,
                Void.class
        );

        // Verify order is APPROVED
        OrderQuery approvedOrder = getOrder(orderId);
        assertThat(approvedOrder.status()).isEqualTo(OrderStatus.APPROVED);

        // when
        // Try to approve the order second time
        var secondApproveResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/approve",
                null,
                ErrorResponse.class
        );

        // then
        assertThat(secondApproveResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(secondApproveResponse.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("incorrect state");
    }

    @Test
    @DisplayName("""
            given order with incorrect price,
            when request is sent,
            then HTTP 400 Bad Request is returned with appropriate error message""")
    void givenOrderWithIncorrectPrice_whenRequestIsSent_thenHttp400BadRequestReturned() {
        // given
        UUID customerId = UUID.randomUUID();
        customerViewService.onCreateCustomer(new CustomerView(customerId));
        UUID productId = UUID.randomUUID();

        OrderAddressRequest address = new OrderAddressRequest(
                "Main Street",
                "12-345",
                "New York",
                "42A"
        );

        List<BasketItemRequest> basketItems = List.of(
                new BasketItemRequest(productId, BigDecimal.valueOf(19.99), 2, BigDecimal.valueOf(39.98))
        );

        // Incorrect total price (should be 39.98)
        BigDecimal incorrectTotalPrice = BigDecimal.valueOf(50.00);

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                address,
                incorrectTotalPrice,
                basketItems
        );

        // when
        var response = testRestTemplate.postForEntity(getBaseOrdersUrl(), request, ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("different than basket items total");
    }

    @Test
    @DisplayName("""
            given already paid order,
            when payment request is sent again,
            then HTTP 400 Bad Request is returned with state error message""")
    void givenAlreadyPaidOrder_whenPaymentRequestIsSentAgain_thenHttp400BadRequestReturned() {
        // given
        UUID orderId = createOrder();

        // Pay the order first time
        testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/pay",
                null,
                Void.class
        );

        // when
        // Try to pay the order second time
        var secondPayResponse = testRestTemplate.postForEntity(
                getBaseOrdersUrl() + "/" + orderId + "/pay",
                null,
                ErrorResponse.class
        );

        // then
        assertThat(secondPayResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(secondPayResponse.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("incorrect state");
    }

    private UUID createOrder() {
        UUID customerId = UUID.randomUUID();
        customerViewService.onCreateCustomer(new CustomerView(customerId));
        UUID productId = UUID.randomUUID();

        OrderAddressRequest address = new OrderAddressRequest(
                "Main Street",
                "12-345",
                "New York",
                "42A"
        );

        List<BasketItemRequest> basketItems = List.of(
                new BasketItemRequest(productId, BigDecimal.valueOf(19.99), 2, BigDecimal.valueOf(39.98))
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customerId,
                address,
                BigDecimal.valueOf(39.98),
                basketItems
        );

        ResponseEntity<Void> createResponse = testRestTemplate.postForEntity(getBaseOrdersUrl(), createRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();
        String orderPath = location.getPath();
        return UUID.fromString(orderPath.substring(orderPath.lastIndexOf('/') + 1));
    }

    private OrderQuery getOrder(UUID orderId) {
        ResponseEntity<OrderQuery> response = testRestTemplate.getForEntity(
                getBaseOrdersUrl() + "/" + orderId,
                OrderQuery.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        OrderQuery order = response.getBody();
        assertThat(order).isNotNull();
        return order;
    }

    String getBaseOrdersUrl() {
        return getBaseUrl("/api/orders");
    }

}
