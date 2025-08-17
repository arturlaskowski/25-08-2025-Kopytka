package pl.kopytka.order.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.KafkaIntegrationTest;
import pl.kopytka.common.web.ErrorResponse;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.application.replicaiton.CustomerView;
import pl.kopytka.order.application.replicaiton.CustomerViewService;
import pl.kopytka.order.web.dto.BasketItemRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.OrderAddressRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
        UUID restaurantId = UUID.randomUUID();
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
                restaurantId,
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
        assertThat(order.restaurantId()).isEqualTo(restaurantId);
        assertThat(order.price()).isEqualByComparingTo(totalPrice);
        assertThat(order.basketItems()).hasSize(1);
        assertThat(order.basketItems().getFirst().productId()).isEqualTo(productId);
        assertThat(order.basketItems().getFirst().price()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(order.basketItems().getFirst().quantity()).isEqualTo(2);
    }


    @Test
    @DisplayName("""
            given order with incorrect price,
            when request is sent,
            then HTTP 400 Bad Request is returned with appropriate error message""")
    void givenOrderWithIncorrectPrice_whenRequestIsSent_thenHttp400BadRequestReturned() {
        // given
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
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
                restaurantId,
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

    String getBaseOrdersUrl() {
        return getBaseUrl("/api/orders");
    }

}
