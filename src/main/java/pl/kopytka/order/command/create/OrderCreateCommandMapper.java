package pl.kopytka.order.command.create;

import org.mapstruct.Mapper;
import pl.kopytka.common.domain.Money;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;
import pl.kopytka.order.domain.Quantity;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
interface OrderCreateCommandMapper {

    List<OrderItem> toOrderItems(List<CreateOrderItemDto> itemDtos);

    OrderAddress toOrderAddress(CreateOrderAddressDto addressDto);

    default Money map(BigDecimal value) {
        return value != null ? new Money(value) : null;
    }

    default Quantity map(Integer value) {
        return value != null ? new Quantity(value) : null;
    }
}