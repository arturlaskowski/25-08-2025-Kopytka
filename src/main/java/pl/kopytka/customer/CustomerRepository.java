package pl.kopytka.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.common.domain.CustomerId;

public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    boolean existsByEmail(String email);
}
