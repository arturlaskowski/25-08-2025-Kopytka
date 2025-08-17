package pl.kopytka.order.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.command.CommandHandler;
import pl.kopytka.order.command.OrderNotFoundException;
import pl.kopytka.order.command.OrderRepository;

@Service
@RequiredArgsConstructor
public class ApproveOrderHandler implements CommandHandler<ApproveOrderCommand> {

    private final OrderRepository orderRepository;

    //TODO restaurant should be able to approve order
    @Transactional
    public void handle(ApproveOrderCommand approveOrderCommand) {
        var order = orderRepository.findById(approveOrderCommand.orderId())
                .orElseThrow(() -> new OrderNotFoundException(approveOrderCommand.orderId()));

        order.approve();
    }
}
