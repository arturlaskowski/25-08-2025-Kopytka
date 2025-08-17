package pl.kopytka.order.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.kopytka.common.command.CommandHandler;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.domain.Money;
import pl.kopytka.common.event.OrderChangedEvent;
import pl.kopytka.order.command.OrderRepository;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.replication.CustomerViewService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderRepository orderRepository;
    private final OrderCreateCommandMapper orderCreateCommandMapper;
    private final CustomerViewService customerViewService;
    private final ApplicationEventPublisher eventPublisher;

    public void handle(CreateOrderCommand createOrderCommand) {
        validateCustomerExists(createOrderCommand.customerId());
        var items = orderCreateCommandMapper.toOrderItems(createOrderCommand.basketItems());
        var orderAddress = orderCreateCommandMapper.toOrderAddress(createOrderCommand.address());

        var order = new Order(createOrderCommand.orderId(),
                new CustomerId(createOrderCommand.customerId()),
                new Money(createOrderCommand.price()),
                items, orderAddress);

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderChangedEvent(
                order.getId().id(),
                order.getCustomerId().id(),
                order.getStatus().name(),
                order.getPrice().amount(),
                order.getCreateAt()));
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerViewService.existsById(customerId)) {
            throw new InvalidOrderException(customerId);
        }
    }
}
