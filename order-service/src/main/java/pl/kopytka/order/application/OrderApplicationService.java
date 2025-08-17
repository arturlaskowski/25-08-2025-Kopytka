package pl.kopytka.order.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.valueobject.*;
import pl.kopytka.order.application.dto.CreateOrderCommand;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.application.exception.InvalidOrderException;
import pl.kopytka.order.application.exception.OrderNotFoundException;
import pl.kopytka.order.application.replicaiton.CustomerViewService;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerViewService customerViewService;
    private final ProcessPaymentCommandPublisher processPaymentCommandPublisher;

    @Transactional
    public OrderId createOrder(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.customerId());

        // Verify customer exists via Customer Service
        CustomerId customerId = new CustomerId(command.customerId());
        if (!customerViewService.existsByCustomerId(customerId.id())) {
            throw new InvalidOrderException("Customer does not exist: " + customerId.id());
        }

        // Convert command to domain objects
        OrderAddress deliveryAddress = new OrderAddress(
                command.deliveryAddress().street(),
                command.deliveryAddress().postCode(),
                command.deliveryAddress().city(),
                command.deliveryAddress().houseNo()
        );

        Set<OrderItem> basketItems = command.basketItems().stream()
                .map(item -> new OrderItem(
                        new ProductId(item.productId()),
                        new Money(item.price()),
                        new Quantity(item.quantity()),
                        new Money(item.totalPrice())
                ))
                .collect(Collectors.toSet());

        Order order = new Order(
                customerId,
                deliveryAddress,
                new Money(command.price()),
                basketItems
        );

        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    @Transactional
    public void payOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        processPaymentCommandPublisher.publishProcessPaymentCommand(orderId, order.getCustomerId(), order.getPrice());
        order.pay();
    }

    //TODO restaurant should be able to approve order
    @Transactional
    public void approveOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.approve();
    }

    @Transactional(readOnly = true)
    public OrderQuery getOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderMapper.toProjection(order);
    }
}
