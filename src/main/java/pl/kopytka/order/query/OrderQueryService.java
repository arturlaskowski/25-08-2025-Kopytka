package pl.kopytka.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.order.command.OrderNotFoundException;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.web.dto.GetOrderByIdQuery;
import pl.kopytka.order.web.dto.OrderPageQuery;
import pl.kopytka.order.web.dto.TrackingOrderQuery;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    public GetOrderByIdQuery getOrderById(UUID id) {
        var orderId = new OrderId(id);
        return orderQueryRepository.findById(orderId)
                .map(OrderQueryMapper::mapToGetOrderByIdQuery)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public TrackingOrderQuery trackOrder(UUID id) {
        var orderId = new OrderId(id);
        return orderQueryRepository.findById(new OrderId(id))
                .map(this::convertToTrackingOrderQuery)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public Page<OrderPageQuery> findAllOrders(Pageable pageable) {
        return orderQueryRepository.findAll(pageable)
                .map(this::convertToOrderPageQuery);
    }

    public Page<OrderPageQuery> findOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderQueryRepository.findAllByStatus(status, pageable);
    }

    private OrderPageQuery convertToOrderPageQuery(Order order) {
        return new OrderPageQuery(order.getId().id(), order.getCreateAt(),
                order.getStatus(), order.getPrice().amount());
    }

    private TrackingOrderQuery convertToTrackingOrderQuery(Order order) {
        return new TrackingOrderQuery(order.getId().id(), order.getStatus(), order.getPrice().amount());
    }
}
