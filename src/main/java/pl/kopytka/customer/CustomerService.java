package pl.kopytka.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.event.CustomerChangedEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        var savedCustomer = customerRepository.save(customer);

        eventPublisher.publishEvent(new CustomerChangedEvent(savedCustomer.getCustomerId().id(), savedCustomer.getEmail()));
        return savedCustomer.getCustomerId();
    }
}