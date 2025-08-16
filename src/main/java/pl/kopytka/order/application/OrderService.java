package pl.kopytka.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.CustomerId;
import pl.kopytka.customer.CustomerFacade;
import pl.kopytka.order.application.dto.CreateOrderDto;
import pl.kopytka.order.application.dto.OrderDto;
import pl.kopytka.order.application.exception.InvalidOrderException;
import pl.kopytka.order.application.exception.OrderNotFoundException;
import pl.kopytka.order.domain.Money;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderId;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerFacade customerFacade;

    public OrderId createOrder(CreateOrderDto createOrderDto) {
        validateCustomerExists(createOrderDto.customerId());
        var items = orderMapper.toOrderItems(createOrderDto.basketItems());
        var orderAddress = orderMapper.toOrderAddress(createOrderDto.address());

        var order = new Order(new CustomerId(createOrderDto.customerId()), new Money(createOrderDto.price()),
                items, orderAddress);

        return orderRepository.save(order).getId();
    }

    //TODO
    //Powinno się wykonać po udanej płatności
    @Transactional
    public void pay(OrderId orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.pay();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(OrderId orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return new OrderDto(orderId.id(), order.getCustomerId().id(), order.getPrice().amount(), order.getStatus(),
                orderMapper.toOrderItemDtos(order.getItems()), orderMapper.toOrderAddressDto(order.getAddress()));
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerFacade.existsById(customerId)) {
            throw new InvalidOrderException("Could not find customer with customerId" + customerId);
        }
    }
}