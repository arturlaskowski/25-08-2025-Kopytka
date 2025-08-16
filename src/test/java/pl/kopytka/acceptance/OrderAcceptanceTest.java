
package pl.kopytka.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kopytka.application.CustomerRepository;
import pl.kopytka.application.OrderRepository;
import pl.kopytka.application.dto.CreateOrderAddressDto;
import pl.kopytka.application.dto.CreateOrderDto;
import pl.kopytka.application.dto.CreateOrderItemDto;
import pl.kopytka.domain.*;
import pl.kopytka.web.exception.ErrorResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("""
            given request to add order for existing customer,
            when request is sent,
            then save order and HTTP 200 status received""")
    void givenRequestToAddOrderForExistingCustomer_whenRequestIsSent_thenOrderSavedAndHttp200() {
        // given
        var createOrderDto = createOrderWitchCustomerDto();

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl(), createOrderDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        var orderId = response.getHeaders().getLocation().getPath().split("/")[3];

        var savedOrder = orderRepository.findById(new OrderId(UUID.fromString(orderId))).orElseThrow();
        assertThat(savedOrder)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("customer.customerId", new CustomerId(createOrderDto.customerId()))
                .hasFieldOrPropertyWithValue("price", new Money(createOrderDto.price()))
                .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                .extracting(Order::getAddress)
                .hasFieldOrPropertyWithValue("street", createOrderDto.address().street())
                .hasFieldOrPropertyWithValue("postCode", createOrderDto.address().postCode())
                .hasFieldOrPropertyWithValue("city", createOrderDto.address().city())
                .hasFieldOrPropertyWithValue("houseNo", createOrderDto.address().houseNo());

        assertThat(savedOrder.getItems()).hasSize(createOrderDto.basketItems().size())
                .zipSatisfy(createOrderDto.basketItems(), (orderItem, orderItemDto) -> {
                    assertThat(orderItem.getProductId().productId()).isEqualTo(orderItemDto.productId());
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
        var notExistingCustomerId = UUID.randomUUID();
        var createOrderDto = createOrderDto(notExistingCustomerId);

        // when
        var response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(createOrderDto),
                ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("Could not find customer with customerId");
    }

    private CreateOrderDto createOrderWitchCustomerDto() {
        var customerId = customerRepository.save(new Customer("Waldek", "Kiepski", "waldek@gmail.com")).getCustomerId().id();

        return createOrderDto(customerId);
    }

    private CreateOrderDto createOrderDto(UUID customerId) {
        var items = List.of(new CreateOrderItemDto(UUID.randomUUID(), 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new CreateOrderItemDto(UUID.randomUUID(), 1, new BigDecimal("34.56"), new BigDecimal("34.56")));
        var address = new CreateOrderAddressDto("Małysza", "94-000", "Adasiowo", "12");
        return new CreateOrderDto(customerId, new BigDecimal("54.56"), items, address);
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/orders";
    }
}
