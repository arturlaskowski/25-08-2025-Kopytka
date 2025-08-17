package pl.kopytka.order.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kopytka.common.command.CommandHandler;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.domain.Money;
import pl.kopytka.customer.CustomerFacade;
import pl.kopytka.order.command.OrderRepository;
import pl.kopytka.order.domain.Order;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderRepository orderRepository;
    private final OrderCreateCommandMapper orderCreateCommandMapper;
    private final CustomerFacade customerFacade;

    public void handle(CreateOrderCommand createOrderCommand) {
        validateCustomerExists(createOrderCommand.customerId());
        var items = orderCreateCommandMapper.toOrderItems(createOrderCommand.basketItems());
        var orderAddress = orderCreateCommandMapper.toOrderAddress(createOrderCommand.address());

        var order = new Order(createOrderCommand.orderId(),
                new CustomerId(createOrderCommand.customerId()),
                new Money(createOrderCommand.price()),
                items, orderAddress);

        orderRepository.save(order);
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerFacade.existsById(customerId)) {
            throw new InvalidOrderException(customerId);
        }
    }
}
