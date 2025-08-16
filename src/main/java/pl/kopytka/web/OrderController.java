package pl.kopytka.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kopytka.application.OrderService;
import pl.kopytka.application.dto.OrderDto;
import pl.kopytka.domain.OrderId;
import pl.kopytka.web.dto.CreateOrderRequest;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderApiMapper orderApiMapper;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest) {
        var createOrderDto = orderApiMapper.toCreateOrderDto(createOrderRequest);
        var orderId = orderService.createOrder(createOrderDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable UUID id) {
        return orderService.getOrderById(new OrderId(id));
    }
}
