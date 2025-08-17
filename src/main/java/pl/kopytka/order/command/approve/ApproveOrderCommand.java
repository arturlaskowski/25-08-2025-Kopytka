package pl.kopytka.order.command.approve;

import jakarta.validation.constraints.NotNull;
import pl.kopytka.common.command.Command;
import pl.kopytka.common.domain.OrderId;

public record ApproveOrderCommand(
        @NotNull OrderId orderId) implements Command {
}
