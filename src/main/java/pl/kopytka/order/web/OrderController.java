package pl.kopytka.order.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.order.application.command.OrderCommandService;
import pl.kopytka.order.application.query.OrderQueryService;
import pl.kopytka.order.domain.OrderStatus;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.GetOrderByIdQuery;
import pl.kopytka.order.web.dto.OrderPageQuery;
import pl.kopytka.order.web.dto.TrackingOrderQuery;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderApiMapper orderApiMapper;

    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest) {
        var createOrderDto = orderApiMapper.toCreateOrderDto(createOrderRequest);
        var orderId = orderCommandService.createOrder(createOrderDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable UUID orderId) {
        orderCommandService.payOrder(new OrderId(orderId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/approve")
    public ResponseEntity<Void> approveOrder(@PathVariable UUID orderId) {
        orderCommandService.approveOrder(new OrderId(orderId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public GetOrderByIdQuery getOrderById(@PathVariable UUID id) {
        return orderQueryService.getOrderById(id);
    }

    @GetMapping("/{id}/track")
    public TrackingOrderQuery trackOrder(@PathVariable UUID id) {
        return orderQueryService.trackOrder(id);
    }

    @GetMapping
    public ResponseEntity<Page<OrderPageQuery>> getAllOrders(
            @SortDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderPageQuery> orders = orderQueryService.findAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<OrderPageQuery>> getPendingOrders(
            @SortDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderPageQuery> pendingOrders = orderQueryService.findOrdersByStatus(OrderStatus.PENDING, pageable);
        return ResponseEntity.ok(pendingOrders);
    }
}
