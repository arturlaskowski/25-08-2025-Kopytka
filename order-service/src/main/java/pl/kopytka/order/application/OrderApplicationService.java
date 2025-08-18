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
import pl.kopytka.order.domain.OrderEventPublisher;
import pl.kopytka.order.domain.OrderItem;
import pl.kopytka.order.domain.event.*;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerViewService customerViewService;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderId createOrder(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.customerId());

        // Verify customer exists via Customer Service
        CustomerId customerId = new CustomerId(command.customerId());
        if (!customerViewService.existsByCustomerId(customerId.id())) {
            throw new InvalidOrderException("Customer does not exist: " + customerId.id());
        }

        var order = mapToOrder(command, customerId);
        var saveOrder = orderRepository.save(order);

        orderEventPublisher.publish(new OrderCreatedEvent(order));

        return saveOrder.getId();
    }

    @Transactional
    public void payOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.pay();
        orderEventPublisher.publish(new OrderPaidEvent(order));
    }

    @Transactional
    public void approveOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.approve();
        orderEventPublisher.publish(new OrderApprovedEvent(order));
    }

    @Transactional
    public void cancelOrder(OrderId orderId, String failureMessage) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel(failureMessage);
        orderEventPublisher.publish(new OrderCanceledEvent(order));
    }

    @Transactional
    public void initCancelOrder(OrderId orderId, String failureMessage) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.initCancel(failureMessage);
        orderEventPublisher.publish(new OrderCancelInitiatedEvent(order));
    }

    @Transactional(readOnly = true)
    public OrderQuery getOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderMapper.toProjection(order);
    }


    private Order mapToOrder(CreateOrderCommand command, CustomerId customerId) {
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

        return new Order(
                customerId,
                new RestaurantId(command.restaurantId()),
                deliveryAddress,
                new Money(command.price()),
                basketItems
        );
    }

}
