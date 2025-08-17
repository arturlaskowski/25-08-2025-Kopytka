package pl.kopytka.customer;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pl.kopytka.customer.application.integration.payment.PaymentServiceClient;
import pl.kopytka.customer.domain.CustomerId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * Test configuration to mock external service clients for acceptance tests
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PaymentServiceClient mockPaymentServiceClient() {
        PaymentServiceClient mock = Mockito.mock(PaymentServiceClient.class);
        // Mock the createWallet method to do nothing (success case)
        doNothing().when(mock).createWallet(any(CustomerId.class));

        return mock;
    }
}
