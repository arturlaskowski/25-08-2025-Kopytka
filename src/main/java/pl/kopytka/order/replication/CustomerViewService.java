package pl.kopytka.order.replication;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.kopytka.common.event.CustomerChangedEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerViewService {

    private final CustomerViewRepository customerViewRepository;

    @EventListener
    public void replicateCustomerView(CustomerChangedEvent event) {
        var customerView = new CustomerView(event.customerId());
        customerViewRepository.save(customerView);
    }

    public boolean existsById(UUID id) {
        return customerViewRepository.existsById(id);
    }

}
