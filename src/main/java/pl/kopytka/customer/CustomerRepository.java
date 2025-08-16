package pl.kopytka.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.common.CustomerId;

interface CustomerRepository extends JpaRepository<Customer, CustomerId> {

    boolean existsByEmail(String email);
}
