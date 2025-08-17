package pl.kopytka.order.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.valueobject.*;
import pl.kopytka.order.application.dto.CreateOrderCommand;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.application.exception.OrderNotFoundException;
import pl.kopytka.order.application.integration.customer.CustomerServiceClient;
import pl.kopytka.order.application.integration.payment.PaymentServiceClient;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerServiceClient customerServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderApplicationService self;

    public OrderApplicationService(OrderRepository orderRepository,
                                   OrderMapper orderMapper,
                                   CustomerServiceClient customerServiceClient,
                                   PaymentServiceClient paymentServiceClient,
                                   @Lazy OrderApplicationService self) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.customerServiceClient = customerServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.self = self;
    }

    @Transactional
    public OrderId createOrder(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.customerId());

        // Verify customer exists via Customer Service
        CustomerId customerId = new CustomerId(command.customerId());
        customerServiceClient.verifyCustomerExists(customerId);

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

        order.pay();

        try {
            // Process payment via Payment Service
            paymentServiceClient.processPayment(
                    orderId,
                    order.getCustomerId(),
                    order.getPrice()
            );
            log.info("Payment successful for order: {}", orderId.id());
        } catch (Exception e) {
            self.failOrder(orderId, e.getMessage());
            log.error("Payment failed for order: {}, reason: {}", orderId.id(), e.getMessage());
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failOrder(OrderId orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.fail(reason);
        log.info("Order {} failed: {}", orderId.id(), reason);
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
