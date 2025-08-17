package pl.kopytka.order.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kopytka.order.domain.Order;
import pl.kopytka.common.domain.valueobject.OrderId;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {
}
