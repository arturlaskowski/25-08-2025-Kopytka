package pl.kopytka.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kopytka.common.domain.CustomerId;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerFacade {

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

    @Override
    public boolean existsById(UUID id) {
        CustomerId customerId = new CustomerId(id);
        return customerRepository.existsById(customerId);
    }
}