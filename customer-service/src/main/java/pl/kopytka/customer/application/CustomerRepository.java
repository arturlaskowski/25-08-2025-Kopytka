package pl.kopytka.customer.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.customer.domain.Customer;
import pl.kopytka.common.domain.valueobject.CustomerId;

public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    boolean existsByEmail(String email);
}
