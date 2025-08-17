package pl.kopytka.order.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kopytka.order.application.dto.BasketItemQuery;
import pl.kopytka.order.application.dto.OrderAddressQuery;
import pl.kopytka.order.application.dto.OrderQuery;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "id.orderId")
    @Mapping(target = "customerId", source = "customerId.customerId")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "basketItems", source = "basket")
    OrderQuery toProjection(Order order);

    @Mapping(target = "productId", source = "productId.productId")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "quantity", source = "quantity.value")
    @Mapping(target = "totalPrice", source = "totalPrice.amount")
    BasketItemQuery toProjection(OrderItem basketItem);

    OrderAddressQuery toProjection(OrderAddress orderAddress);
}