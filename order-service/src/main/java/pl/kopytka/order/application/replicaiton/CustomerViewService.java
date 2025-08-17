package pl.kopytka.order.application.replicaiton;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerViewService {

    private final CustomerViewRepository repository;

    public void onCreateCustomer(CustomerView customerView) {
        repository.save(customerView);
    }

    public boolean existsByCustomerId(UUID customerId) {
        return repository.existsById(customerId);
    }
}
