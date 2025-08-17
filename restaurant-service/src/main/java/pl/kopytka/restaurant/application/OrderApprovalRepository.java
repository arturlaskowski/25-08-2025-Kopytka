package pl.kopytka.restaurant.application;

import org.springframework.data.repository.CrudRepository;
import pl.kopytka.restaurant.domain.entity.OrderApproval;
import pl.kopytka.restaurant.domain.entity.OrderApprovalId;

public interface OrderApprovalRepository extends CrudRepository<OrderApproval, OrderApprovalId> {
}
