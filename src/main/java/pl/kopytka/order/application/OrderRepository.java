package pl.kopytka.order.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderId;

public interface OrderRepository extends JpaRepository<Order, OrderId> {

}
