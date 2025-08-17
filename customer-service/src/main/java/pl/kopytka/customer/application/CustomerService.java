package pl.kopytka.customer.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.customer.application.dto.CustomerDto;
import pl.kopytka.customer.application.exception.CustomerAlreadyExistsException;
import pl.kopytka.customer.application.exception.CustomerNotFoundException;
import pl.kopytka.customer.application.integration.payment.PaymentServiceClient;
import pl.kopytka.customer.application.dto.CreateCustomerDto;
import pl.kopytka.customer.domain.Customer;
import pl.kopytka.customer.domain.CustomerId;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PaymentServiceClient paymentServiceClient;

    public CustomerDto getCustomer(UUID id) {
        CustomerId customerId = new CustomerId(id);
        return customerRepository.findById(customerId)
                .map(customer -> new CustomerDto(customer.getCustomerId().id(), customer.getFirstName(), customer.getLastName(), customer.getEmail()))
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public CustomerId addCustomer(CreateCustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.email())) {
            throw new CustomerAlreadyExistsException(customerDto.email());
        }
        
        // Create the customer
        var customer = new Customer(customerDto.firstName(), customerDto.lastName(), customerDto.email());
        CustomerId customerId = customerRepository.save(customer).getCustomerId();
        
        // Create a wallet for the customer via the payment service
        paymentServiceClient.createWallet(customerId);
        
        return customerId;
    }
}