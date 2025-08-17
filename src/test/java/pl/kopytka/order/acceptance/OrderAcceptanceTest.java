
package pl.kopytka.order.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.domain.Money;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.common.web.ErrorResponse;
import pl.kopytka.order.command.OrderRepository;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.domain.Quantity;
import pl.kopytka.order.web.dto.CreateOrderRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(OrderTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StubCustomerFacade stubCustomerFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("""
            given request to add order for existing customer,
            when request is sent,
            then save order and HTTP 201 status received""")
    void givenRequestToAddOrderForExistingCustomer_whenRequestIsSent_thenOrderSavedAndHttp201() {
        // given
        var createOrderDto = createOrderCommand();

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl(), createOrderDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();
        var orderId = UUID.fromString(UriComponentsBuilder.fromUri(response.getHeaders().getLocation()).build()
                .getPathSegments().getLast());

        var savedOrder = orderRepository.findById(new OrderId(orderId)).orElseThrow();
        assertThat(savedOrder)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("customerId", new CustomerId(createOrderDto.customerId()))
                .hasFieldOrPropertyWithValue("price", new Money(createOrderDto.price()))
                .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                .extracting(Order::getAddress)
                .hasFieldOrPropertyWithValue("street", createOrderDto.deliveryAddress().street())
                .hasFieldOrPropertyWithValue("postCode", createOrderDto.deliveryAddress().postCode())
                .hasFieldOrPropertyWithValue("city", createOrderDto.deliveryAddress().city())
                .hasFieldOrPropertyWithValue("houseNo", createOrderDto.deliveryAddress().houseNo());

        assertThat(savedOrder.getItems()).hasSize(createOrderDto.basketItems().size())
                .zipSatisfy(createOrderDto.basketItems(), (orderItem, orderItemDto) -> {
                    assertThat(orderItem.getProductId()).isEqualTo(orderItemDto.productId());
                    assertThat(orderItem.getPrice()).isEqualTo(new Money(orderItemDto.price()));
                    assertThat(orderItem.getQuantity()).isEqualTo(new Quantity(orderItemDto.quantity()));
                    assertThat(orderItem.getTotalPrice()).isEqualTo(new Money(orderItemDto.totalPrice()));
                });
    }

    @Test
    @DisplayName("""
            given request to add order for non-existing customer,
            when request is sent,
            then HTTP 400 status received and error message returned""")
    void givenRequestToAddOrderForNonExistingCustomer_whenRequestIsSent_thenHttp400AndErrorMessage() {
        // given
        var createOrderDto = createOrderCommand();
        stubCustomerFacade.setCustomerExists(false);

        // when
        var response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(createOrderDto),
                ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("Could not find customer with customerId");

        // Reset stub for other tests
        stubCustomerFacade.setCustomerExists(true);
    }

    private CreateOrderRequest createOrderCommand() {
        var items = List.of(new CreateOrderRequest.OrderItemRequest(UUID.randomUUID(), 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new CreateOrderRequest.OrderItemRequest(UUID.randomUUID(), 1, new BigDecimal("34.56"), new BigDecimal("34.56")));
        var address = new CreateOrderRequest.OrderAddressRequest("Małysza", "94-000", "Adasiowo", "12");
        return new CreateOrderRequest(UUID.randomUUID(), new BigDecimal("54.56"), items, address);
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/orders";
    }
}
