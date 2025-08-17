package pl.kopytka.order.command.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pl.kopytka.common.command.Command;
import pl.kopytka.common.domain.OrderId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        @NotNull OrderId orderId,
        @NotNull UUID customerId,
        @NotNull @Min(0) BigDecimal price,
        @Valid @NotNull List<CreateOrderItemDto> basketItems,
        @Valid CreateOrderAddressDto address
) implements Command {
}