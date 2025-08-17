package pl.kopytka.order.application.integration.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.web.ServiceUnavailableException;
import pl.kopytka.common.web.dto.CustomerResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.customer.url}")
    private String customerServiceUrl;

    public void verifyCustomerExists(CustomerId customerId) {
        try {
            String url = customerServiceUrl + "/api/customers/" + customerId.id();
            log.info("Verifying customer existence: {}", customerId.id());

            restTemplate.getForEntity(url, CustomerResponse.class);

            // No need to check status code - RestTemplate throws exceptions for non-2xx responses
            log.info("Customer verified successfully: {}", customerId.id());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Customer not found: {}", customerId.id());
            throw new CustomerNotFoundException(customerId);
        } catch (Exception e) {
            log.error("Error while verifying customer existence: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Customer", "verifying customer", e);
        }
    }
}
