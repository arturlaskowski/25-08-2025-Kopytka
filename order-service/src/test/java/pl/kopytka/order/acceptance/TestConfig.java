package pl.kopytka.order.acceptance;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pl.kopytka.order.application.integration.customer.CustomerServiceClient;
import pl.kopytka.order.application.integration.payment.PaymentServiceClient;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * Test configuration to mock external service clients for acceptance tests
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CustomerServiceClient mockCustomerServiceClient() {
        CustomerServiceClient mock = Mockito.mock(CustomerServiceClient.class);
        // Mock the verifyCustomerExists method to do nothing (success case)
        doNothing().when(mock).verifyCustomerExists(any(CustomerId.class));
        return mock;
    }

    @Bean
    @Primary
    public PaymentServiceClient mockPaymentServiceClient() {
        PaymentServiceClient mock = Mockito.mock(PaymentServiceClient.class);
        // Mock the processPayment method to not throw exceptions (success case)
        doNothing().when(mock).processPayment(
                any(OrderId.class),
                any(CustomerId.class),
                any(Money.class)
        );
        
        return mock;
    }
}
