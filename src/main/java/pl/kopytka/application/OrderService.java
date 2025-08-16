package pl.kopytka.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.application.dto.CreateOrderDto;
import pl.kopytka.application.dto.OrderDto;
import pl.kopytka.application.exception.CustomerNotFoundException;
import pl.kopytka.application.exception.OrderNotFoundException;
import pl.kopytka.domain.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    public OrderId createOrder(CreateOrderDto createOrderDto) {
        var customer = findCustomerById(createOrderDto.customerId());
        var items = orderMapper.toOrderItems(createOrderDto.basketItems());
        var orderAddress = orderMapper.toOrderAddress(createOrderDto.address());

        var order = new Order(customer, new Money(createOrderDto.price()),
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
        return new OrderDto(orderId.id(), order.getCustomer().getCustomerId().id(), order.getPrice().amount(), order.getStatus(),
                orderMapper.toOrderItemDtos(order.getItems()), orderMapper.toOrderAddressDto(order.getAddress()));
    }

    private Customer findCustomerById(UUID customerId) {
        return customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}