package pl.kopytka.order.application.integration.customer;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.web.ServiceUnavailableException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    private final CustomerServiceFeignClient customerServiceFeignClient;

    public void verifyCustomerExists(CustomerId customerId) {
        try {
            log.info("Verifying customer existence: {}", customerId.id());

            customerServiceFeignClient.getCustomer(customerId.id());

            log.info("Customer verified successfully: {}", customerId.id());

        } catch (FeignException.NotFound e) {
            log.warn("Customer not found: {}", customerId.id());
            throw new CustomerNotFoundException(customerId);
        } catch (FeignException e) {
            log.error("Error while verifying customer existence: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Customer", "verifying customer", e);
        } catch (Exception e) {
            log.error("Unexpected error while verifying customer existence: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Customer", "verifying customer", e);
        }
    }
}
