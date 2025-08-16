package pl.kopytka.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.domain.Order;
import pl.kopytka.domain.OrderId;

public interface OrderRepository extends JpaRepository<Order, OrderId> {

}
