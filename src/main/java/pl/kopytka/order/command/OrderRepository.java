package pl.kopytka.order.command;

import org.springframework.data.repository.CrudRepository;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.order.domain.Order;

public interface OrderRepository extends CrudRepository<Order, OrderId> {

}
