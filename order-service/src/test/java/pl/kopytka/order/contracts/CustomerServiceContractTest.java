package pl.kopytka.order.contracts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.order.application.integration.customer.CustomerServiceFeignClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.LOCAL;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"eureka.client.enabled=false", "kopytka.scheduling.enabled=false"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = LOCAL,
        ids = "pl.kopytka.now:customer-service:+:stubs:9581")
class CustomerServiceContractTest {

    @Autowired
    private CustomerServiceFeignClient customerServiceFeignClient;

    @Test
    void should_return_true_when_customer_exists() {
        // given
        var customerId = new CustomerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        // when
        var response = customerServiceFeignClient.getCustomer(customerId.id());

        // then
        assertThat(response)
                .hasFieldOrProperty("id")
                .hasFieldOrProperty("firstName")
                .hasFieldOrProperty("lastName")
                .hasFieldOrProperty("email");
    }
}
