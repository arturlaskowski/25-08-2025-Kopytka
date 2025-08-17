package pl.kopytka.order.command.pay;

import jakarta.validation.constraints.NotNull;
import pl.kopytka.common.command.Command;
import pl.kopytka.common.domain.OrderId;

public record PayOrderCommand(
        @NotNull OrderId orderId) implements Command {
}
