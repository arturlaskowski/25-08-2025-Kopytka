package pl.kopytka.order.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.order.application.OrderApplicationService;
import pl.kopytka.order.application.dto.CreateBasketItemDto;
import pl.kopytka.order.application.dto.CreateOrderAddressDto;
import pl.kopytka.order.application.dto.CreateOrderCommand;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.web.dto.CreateOrderRequest;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = mapToCommand(request);
        OrderId orderId = orderApplicationService.createOrder(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderQuery> getOrder(@PathVariable UUID orderId) {
        OrderQuery order = orderApplicationService.getOrder(new OrderId(orderId));
        return ResponseEntity.ok(order);
    }

    private CreateOrderCommand mapToCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.restaurantId(),
                new CreateOrderAddressDto(
                        request.deliveryAddress().street(),
                        request.deliveryAddress().postCode(),
                        request.deliveryAddress().city(),
                        request.deliveryAddress().houseNo()
                ),
                request.price(),
                request.basketItems().stream()
                        .map(item -> new CreateBasketItemDto(
                                item.productId(),
                                item.price(),
                                item.quantity(),
                                item.totalPrice()
                        ))
                        .toList()
        );
    }
}
