package pl.kopytka.order.application.integration.customer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.kopytka.common.web.dto.CustomerResponse;

import java.util.UUID;

@FeignClient(name = "customer-service")
public interface CustomerServiceFeignClient {
    
    @GetMapping("/api/customers/{customerId}")
    CustomerResponse getCustomer(@PathVariable("customerId") UUID customerId);
}
