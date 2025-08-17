package pl.kopytka.customer.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.customer.domain.Customer;


public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    boolean existsByEmail(String email);
}
