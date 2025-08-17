package pl.kopytka.customer.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.customer.CustomerApp;
import pl.kopytka.customer.application.CustomerService;
import pl.kopytka.customer.application.dto.CustomerDto;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CustomerApp.class)
@TestPropertySource(properties = {"eureka.client.enabled=false", "kopytka.scheduling.enabled=false"})
class ContractTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CustomerService customerService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);

        // Mock existing customer
        CustomerDto existingCustomer = new CustomerDto(
                CustomerId.newOne().id(),
                "John",
                "Doe",
                "john.doe@example.com"
        );

        when(customerService.getCustomer(any(UUID.class)))
                .thenReturn(existingCustomer);
    }
}
