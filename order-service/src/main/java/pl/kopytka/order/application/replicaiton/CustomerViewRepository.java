package pl.kopytka.order.application.replicaiton;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CustomerViewRepository extends JpaRepository<CustomerView, UUID> {
}
