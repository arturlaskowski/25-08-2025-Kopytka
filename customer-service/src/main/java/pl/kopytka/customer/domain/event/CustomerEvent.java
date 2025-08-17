package pl.kopytka.customer.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kopytka.common.domain.event.DomainEvent;
import pl.kopytka.customer.domain.Customer;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public abstract class CustomerEvent implements DomainEvent {
    private final Customer customer;
    private final Instant createdAt;

    CustomerEvent(Customer customer) {
        this.customer = customer;
        this.createdAt = Instant.now();
    }
}
