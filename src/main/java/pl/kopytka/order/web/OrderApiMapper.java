package pl.kopytka.order.web;

import org.mapstruct.Mapper;
import pl.kopytka.order.application.dto.CreateOrderAddressDto;
import pl.kopytka.order.application.dto.CreateOrderDto;
import pl.kopytka.order.application.dto.CreateOrderItemDto;
import pl.kopytka.order.web.dto.CreateOrderRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderItemRequest;
import pl.kopytka.order.web.dto.CreateOrderRequest.OrderAddressRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderApiMapper {

    CreateOrderDto toCreateOrderDto(CreateOrderRequest request);

    CreateOrderItemDto toCreateOrderItemDto(OrderItemRequest item);
    
    List<CreateOrderItemDto> toCreateOrderItemDtos(List<OrderItemRequest> basketItems);

    CreateOrderAddressDto toCreateOrderAddressDto(OrderAddressRequest address);
}