package pl.kopytka.order.web;

import org.mapstruct.Mapper;
import pl.kopytka.order.application.command.dto.CreateOrderAddressDto;
import pl.kopytka.order.application.command.dto.CreateOrderCommand;
import pl.kopytka.order.application.command.dto.CreateOrderItemDto;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderAddressRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderItemRequest;

import java.util.List;

@Mapper(componentModel = "spring")
interface OrderApiMapper {

    CreateOrderCommand toCreateOrderDto(CreateOrderRequest request);

    CreateOrderItemDto toCreateOrderItemDto(OrderItemRequest item);

    List<CreateOrderItemDto> toCreateOrderItemDtos(List<OrderItemRequest> basketItems);

    CreateOrderAddressDto toCreateOrderAddressDto(OrderAddressRequest address);
}