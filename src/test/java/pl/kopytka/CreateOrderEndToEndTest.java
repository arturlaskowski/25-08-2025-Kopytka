
package pl.kopytka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import pl.kopytka.common.ExceptionTestUtils;
import pl.kopytka.customer.CreateCustomerDto;
import pl.kopytka.order.command.create.InvalidOrderException;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.GetOrderByIdQuery;
import pl.kopytka.trackorder.TrackingOrderProjection;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateOrderEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("""
            given add customer and add order for existing customer,
            when request is sent,
            then save order and HTTP 200 status received""")
    void givenRequestToAddOrderForExistingCustomer_whenRequestIsSent_thenOrderSavedAndHttp200() {
        //given
        var createCustomerDto = new CreateCustomerDto("Marianek", "Paździoch", "pazdzeik@gemail.com");

        //when - create customer
        var postCustomerResponse = restTemplate.postForEntity(getBaseCustomersUrl(), createCustomerDto, Void.class);
        assertThat(postCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postCustomerResponse.getHeaders().getLocation()).isNotNull();
        var customerId = postCustomerResponse.getHeaders().getLocation().getPath().split("/")[3];

        //when - create order
        var createOrderRequest = createOrderRequest(UUID.fromString(customerId));
        var postOrderResponse = restTemplate.postForEntity(getBaseOrdersUrl(), createOrderRequest, Void.class);
        assertThat(postOrderResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postOrderResponse.getHeaders().getLocation()).isNotNull();

        //when - get order
        var location = postOrderResponse.getHeaders().getLocation();
        var getOrderResponse = restTemplate.getForEntity(location, GetOrderByIdQuery.class);
        assertThat(getOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //then
        assertThat(getOrderResponse.getBody()).isNotNull();

        //then
        var orderResponse = getOrderResponse.getBody();
        assertThat(orderResponse)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("customerId", createOrderRequest.customerId())
                .hasFieldOrPropertyWithValue("price", createOrderRequest.price())
                .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                .extracting(GetOrderByIdQuery::address)
                .hasFieldOrPropertyWithValue("street", createOrderRequest.deliveryAddress().street())
                .hasFieldOrPropertyWithValue("postCode", createOrderRequest.deliveryAddress().postCode())
                .hasFieldOrPropertyWithValue("city", createOrderRequest.deliveryAddress().city())
                .hasFieldOrPropertyWithValue("houseNo", createOrderRequest.deliveryAddress().houseNo());

        assertThat(orderResponse.basketItems()).hasSize(createOrderRequest.basketItems().size())
                .zipSatisfy(createOrderRequest.basketItems(), (getItem, postItem) -> {
                    assertThat(getItem.productId()).isEqualTo(postItem.productId());
                    assertThat(getItem.price()).isEqualTo(postItem.price());
                    assertThat(getItem.quantity()).isEqualTo(postItem.quantity());
                    assertThat(getItem.totalPrice()).isEqualTo(postItem.totalPrice());
                });

        //when - track order
        var orderId = location.getPath().split("/")[3];
        var trackOrderUrl = "http://localhost:" + port + "/api/orders/" + orderId + "/track";
        var trackOrderResponse = restTemplate.getForEntity(trackOrderUrl, TrackingOrderProjection.class);
        assertThat(trackOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //then - verify tracking response
        assertThat(trackOrderResponse.getBody()).isNotNull();
        var trackingData = trackOrderResponse.getBody();
        assertThat(trackingData.getOrderId()).isEqualTo(UUID.fromString(orderId));
        assertThat(trackingData.getStatus()).isEqualTo(OrderStatus.PENDING.name());
    }

    @Test
    @DisplayName("""
            add order for not existing customer,
            when request is sent,
            then order is not saved and HTTP 400 status received""")
    void givenRequestToAddOrderForNotExistingCustomer_whenRequestIsSent_thenOrderNotSavedAndHttp400() {
        //when - create order
        var notExistingCustomerId = UUID.randomUUID();
        var createOrderDto = createOrderRequest(notExistingCustomerId);
        var postOrderResponse = restTemplate.postForEntity(getBaseOrdersUrl(), createOrderDto, Object.class);

        //then
        assertThat(postOrderResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ExceptionTestUtils.assertExceptionMessage(postOrderResponse.getBody(),
                InvalidOrderException.createExceptionMessage(notExistingCustomerId));
    }

    private CreateOrderRequest createOrderRequest(UUID customerId) {
        var items = List.of(new CreateOrderRequest.OrderItemRequest(UUID.randomUUID(), 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new CreateOrderRequest.OrderItemRequest(UUID.randomUUID(), 1, new BigDecimal("34.56"), new BigDecimal("34.56")));
        var address = new CreateOrderRequest.OrderAddressRequest("Małysza", "94-000", "Adasiowo", "12");
        return new CreateOrderRequest(customerId, new BigDecimal("54.56"), items, address);
    }

    private String getBaseOrdersUrl() {
        return "http://localhost:" + port + "/api/orders";
    }

    private String getBaseCustomersUrl() {
        return "http://localhost:" + port + "/api/customers";
    }
}