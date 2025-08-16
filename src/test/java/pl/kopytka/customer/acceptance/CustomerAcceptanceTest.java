package pl.kopytka.customer.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kopytka.common.ErrorResponse;
import pl.kopytka.customer.CustomerService;
import pl.kopytka.customer.CreateCustomerDto;
import pl.kopytka.customer.CustomerDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("""
            given existing Customer id,
            when request is sent,
            then return Customer details and HTTP 200 status""")
    void givenExistingCustomerId_whenRequestIsSent_thenCustomerDetailsReturnedAndHttp200() {
        //given
        var createCustomerDto = new CreateCustomerDto("Ferdzio", "Kiepski", "ferdek@gemail.com");
        var customerId = customerService.addCustomer(createCustomerDto);

        //when
        ResponseEntity<CustomerDto> response = restTemplate.getForEntity(
                getBaseCustomersUrl() + "/" + customerId.id(), CustomerDto.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("id", customerId.id())
                .hasFieldOrPropertyWithValue("firstName", createCustomerDto.firstName())
                .hasFieldOrPropertyWithValue("lastName", createCustomerDto.lastName())
                .hasFieldOrPropertyWithValue("email", createCustomerDto.email());
    }

    @Test
    @DisplayName("""
            given non-existing Customer id,
            when request is sent,
            then do not return Customer details and HTTP 404 status""")
    void givenNonExistingCustomerId_whenRequestIsSent_thenCustomerDetailsNotReturnedAndHttp404() {
        //given
        var notExistingId = UUID.randomUUID();

        //when
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(getBaseCustomersUrl() + "/" + notExistingId, ErrorResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .asString()
                .contains("Could not find customer");
    }

    @Test
    @DisplayName("""
            given request for creating Customer with all mandatory data correctly,
            when request is sent,
            then Customer is added and HTTP 201 status received""")
    void givenRequestForCreatingCustomer_whenRequestIsSent_thenCustomerAddedAndHttp201() {
        //given
        var createCustomerDto = new CreateCustomerDto("Marianek", "Paździoch", "mario@gemail.com");

        //when
        ResponseEntity<Void> postResponse = restTemplate.postForEntity(getBaseCustomersUrl(), createCustomerDto, Void.class);

        //then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var location = postResponse.getHeaders().getLocation();
        var getResponse = restTemplate.getForEntity(location, CustomerDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(getResponse.getBody())
                .hasFieldOrPropertyWithValue("firstName", createCustomerDto.firstName())
                .hasFieldOrPropertyWithValue("lastName", createCustomerDto.lastName())
                .hasFieldOrPropertyWithValue("email", createCustomerDto.email());
    }

    @Test
    @DisplayName("""
            given request for creating Customer with existing email,
            when request is sent,
            then Customer not added and HTTP 409 status received""")
    void givenRequestForCreatingCustomerWithExistingEmail_whenRequestIsSent_thenCustomerNotAddedAndHttp409() {
        //given
        String email = "waldek12@gmail.com";
        customerService.addCustomer(new CreateCustomerDto("Waldemar", "Kiepski", email));

        var createCustomerDto = new CreateCustomerDto("Walduś", "Boczek", email);

        //when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(getBaseCustomersUrl(), createCustomerDto, ErrorResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getLocation()).isNull();
        assertThat(response.getBody())
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .extracting("message")
                .isNotNull();
    }

    private String getBaseCustomersUrl() {
        return "http://localhost:" + port + "/api/customers";
    }
}