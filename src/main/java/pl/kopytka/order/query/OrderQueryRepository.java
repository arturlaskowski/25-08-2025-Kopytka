package pl.kopytka.order.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.web.dto.OrderPageQuery;

interface OrderQueryRepository extends JpaRepository<Order, OrderId>, JpaSpecificationExecutor<Order> {

    @Query("SELECT new pl.kopytka.order.web.dto.OrderPageQuery(o.id.orderId, o.createAt, o.status, o.price.amount)" +
            " FROM pl.kopytka.order.domain.Order o WHERE o.status = :status")
    Page<OrderPageQuery> findAllByStatus(OrderStatus status, Pageable pageable);
}
