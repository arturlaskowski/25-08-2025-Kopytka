package pl.kopytka.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.domain.Customer;
import pl.kopytka.domain.CustomerId;

public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    boolean existsByEmail(String email);
}
