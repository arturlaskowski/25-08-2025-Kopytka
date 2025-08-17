package pl.kopytka.order.web;

import org.mapstruct.Mapper;
import pl.kopytka.common.domain.OrderId;
import pl.kopytka.order.command.create.CreateOrderAddressDto;
import pl.kopytka.order.command.create.CreateOrderCommand;
import pl.kopytka.order.command.create.CreateOrderItemDto;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderAddressRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderItemRequest;

import java.util.List;

@Mapper(componentModel = "spring")
interface OrderApiMapper {

    default CreateOrderCommand toCreateOrderCommand(OrderId orderId, CreateOrderRequest request) {
        return new CreateOrderCommand(
                orderId,
                request.customerId(),
                request.price(),
                toCreateOrderItemDtos(request.basketItems()),
                toCreateOrderAddressDto(request.deliveryAddress())
        );
    }

    CreateOrderItemDto toCreateOrderItemDto(OrderItemRequest item);

    List<CreateOrderItemDto> toCreateOrderItemDtos(List<OrderItemRequest> basketItems);

    CreateOrderAddressDto toCreateOrderAddressDto(OrderAddressRequest address);
}