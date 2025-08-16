
package pl.kopytka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import pl.kopytka.customer.CreateCustomerDto;
import pl.kopytka.order.application.dto.CreateOrderAddressDto;
import pl.kopytka.order.application.dto.CreateOrderDto;
import pl.kopytka.order.application.dto.CreateOrderItemDto;
import pl.kopytka.order.application.dto.OrderDto;
import pl.kopytka.order.domain.OrderStatus;

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
        var postCustomerResponse = restTemplate.postForEntity(getBaseCustomersUrl(), createCustomerDto, UUID.class);
        assertThat(postCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postCustomerResponse.getHeaders().getLocation()).isNotNull();
        var customerId = postCustomerResponse.getHeaders().getLocation().getPath().split("/")[3];

        //when - create order
        var createOrderDto = createOrderDto(UUID.fromString(customerId));
        var postOrderResponse = restTemplate.postForEntity(getBaseOrdersUrl(), createOrderDto, UUID.class);
        assertThat(postOrderResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postOrderResponse.getHeaders().getLocation()).isNotNull();

        //when - get order
        var location = postOrderResponse.getHeaders().getLocation();
        var getOrderResponse = restTemplate.getForEntity(location, OrderDto.class);
        assertThat(getOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //then
        assertThat(getOrderResponse.getBody()).isNotNull();
        var orderResponse = getOrderResponse.getBody();
        assertThat(orderResponse)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("customerId", createOrderDto.customerId())
                .hasFieldOrPropertyWithValue("price", createOrderDto.price())
                .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                .extracting(OrderDto::address)
                .hasFieldOrPropertyWithValue("street", createOrderDto.address().street())
                .hasFieldOrPropertyWithValue("postCode", createOrderDto.address().postCode())
                .hasFieldOrPropertyWithValue("city", createOrderDto.address().city())
                .hasFieldOrPropertyWithValue("houseNo", createOrderDto.address().houseNo());

        assertThat(orderResponse.basketItems()).hasSize(createOrderDto.basketItems().size())
                .zipSatisfy(createOrderDto.basketItems(), (getItem, postItem) -> {
                    assertThat(getItem.productId()).isEqualTo(postItem.productId());
                    assertThat(getItem.price()).isEqualTo(postItem.price());
                    assertThat(getItem.quantity()).isEqualTo(postItem.quantity());
                    assertThat(getItem.totalPrice()).isEqualTo(postItem.totalPrice());
                });
    }

    @Test
    @DisplayName("""
            add order for not existing customer,
            when request is sent,
            then order is not saved and HTTP 400 status received""")
    void givenRequestToAddOrderForNotExistingCustomer_whenRequestIsSent_thenOrderNotSavedAndHttp400() {
        //when - create order
        var notExistingCustomerId = UUID.randomUUID();
        var createOrderDto = createOrderDto(notExistingCustomerId);
        var postOrderResponse = restTemplate.postForEntity(getBaseOrdersUrl(), createOrderDto, Object.class);

        //then
        assertThat(postOrderResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(postOrderResponse.getBody())
                .isNotNull()
                .extracting("message")
                .asString()
                .contains("Could not find customer");
    }

    private CreateOrderDto createOrderDto(UUID customerId) {
        var items = List.of(new CreateOrderItemDto(UUID.randomUUID(), 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new CreateOrderItemDto(UUID.randomUUID(), 1, new BigDecimal("34.56"), new BigDecimal("34.56")));
        var address = new CreateOrderAddressDto("Małysza", "94-000", "Adasiowo", "12");
        return new CreateOrderDto(customerId, new BigDecimal("54.56"), items, address);
    }

    private String getBaseOrdersUrl() {
        return "http://localhost:" + port + "/api/orders";
    }

    private String getBaseCustomersUrl() {
        return "http://localhost:" + port + "/api/customers";
    }
}