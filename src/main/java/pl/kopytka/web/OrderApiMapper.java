package pl.kopytka.web;

import org.mapstruct.Mapper;
import pl.kopytka.application.dto.CreateOrderAddressDto;
import pl.kopytka.application.dto.CreateOrderDto;
import pl.kopytka.application.dto.CreateOrderItemDto;
import pl.kopytka.web.dto.CreateOrderRequest;
import pl.kopytka.web.dto.CreateOrderRequest.OrderItemRequest;
import pl.kopytka.web.dto.CreateOrderRequest.OrderAddressRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderApiMapper {

    CreateOrderDto toCreateOrderDto(CreateOrderRequest request);

    CreateOrderItemDto toCreateOrderItemDto(OrderItemRequest item);
    
    List<CreateOrderItemDto> toCreateOrderItemDtos(List<OrderItemRequest> basketItems);

    CreateOrderAddressDto toCreateOrderAddressDto(OrderAddressRequest address);
}