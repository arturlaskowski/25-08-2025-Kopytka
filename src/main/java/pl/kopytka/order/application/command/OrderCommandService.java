package pl.kopytka.order.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.domain.Money;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.customer.CustomerFacade;
import pl.kopytka.order.application.OrderNotFoundException;
import pl.kopytka.order.application.command.dto.CreateOrderCommand;
import pl.kopytka.order.domain.Order;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderCommandMapper orderCommandMapper;
    private final CustomerFacade customerFacade;

    public OrderId createOrder(CreateOrderCommand createOrderCommand) {
        validateCustomerExists(createOrderCommand.customerId());
        var items = orderCommandMapper.toOrderItems(createOrderCommand.basketItems());
        var orderAddress = orderCommandMapper.toOrderAddress(createOrderCommand.deliveryAddress());

        var order = new Order(new CustomerId(createOrderCommand.customerId()), new Money(createOrderCommand.price()),
                items, orderAddress);

        return orderRepository.save(order).getId();
    }

    //TODO add payment service to handle payments
    @Transactional
    public void payOrder(OrderId orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.pay();
    }

    //TODO restaurant should be able to approve order
    @Transactional
    public void approveOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.approve();
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerFacade.existsById(customerId)) {
            throw new InvalidOrderException(customerId);
        }
    }
}