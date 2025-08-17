package pl.kopytka.customer.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kopytka.customer.application.CustomerService;
import pl.kopytka.customer.web.dto.CreateCustomerRequest;
import pl.kopytka.customer.web.dto.CustomerResponse;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
class CustomerController {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        var customer = customerService.getCustomer(id);
        var customerResponse = customerApiMapper.toCustomerResponse(customer);
        return ResponseEntity.ok(customerResponse);
    }

    @PostMapping
    public ResponseEntity<Void> addCustomer(@RequestBody @Valid CreateCustomerRequest createCustomerRequest) {
        var createCustomerDto = customerApiMapper.toCreateCustomerDto(createCustomerRequest);
        var customerId = customerService.addCustomer(createCustomerDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customerId.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
