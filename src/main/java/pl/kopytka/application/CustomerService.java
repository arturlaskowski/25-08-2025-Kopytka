package pl.kopytka.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kopytka.application.dto.CreateCustomerDto;
import pl.kopytka.application.dto.CustomerDto;
import pl.kopytka.application.exception.CustomerAlreadyExistsException;
import pl.kopytka.application.exception.CustomerNotFoundException;
import pl.kopytka.domain.Customer;
import pl.kopytka.domain.CustomerId;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerDto getCustomer(UUID id) {
        CustomerId customerId = new CustomerId(id);
        return customerRepository.findById(customerId)
                .map(customer -> new CustomerDto(customer.getCustomerId().id(), customer.getFirstName(), customer.getLastName(), customer.getEmail()))
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public CustomerId addCustomer(CreateCustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.email())) {
            throw new CustomerAlreadyExistsException(customerDto.email());
        }
        var customer = new Customer(customerDto.firstName(), customerDto.lastName(), customerDto.email());
        return customerRepository.save(customer).getCustomerId();
    }
}