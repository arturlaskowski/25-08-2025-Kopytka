package pl.kopytka.order.command.pay;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.command.CommandHandler;
import pl.kopytka.common.event.OrderChangedEvent;
import pl.kopytka.order.command.OrderNotFoundException;
import pl.kopytka.order.command.OrderRepository;

@Service
@RequiredArgsConstructor
public class PayOrderHandler implements CommandHandler<PayOrderCommand> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    //TODO add payment service to handle payments
    @Transactional
    public void handle(PayOrderCommand payOrderCommand) {
        var order = orderRepository.findById(payOrderCommand.orderId())
                .orElseThrow(() -> new OrderNotFoundException(payOrderCommand.orderId()));

        order.pay();

        eventPublisher.publishEvent(new OrderChangedEvent(
                order.getId().id(),
                order.getCustomerId().id(),
                order.getStatus().name(),
                order.getPrice().amount(),
                order.getCreateAt()));
    }
}
