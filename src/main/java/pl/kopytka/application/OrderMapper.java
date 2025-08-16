package pl.kopytka.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.kopytka.application.dto.CreateOrderAddressDto;
import pl.kopytka.application.dto.CreateOrderItemDto;
import pl.kopytka.application.dto.OrderAddressDto;
import pl.kopytka.application.dto.OrderItemDto;
import pl.kopytka.domain.Money;
import pl.kopytka.domain.OrderAddress;
import pl.kopytka.domain.OrderItem;
import pl.kopytka.domain.ProductId;
import pl.kopytka.domain.Quantity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    List<OrderItem> toOrderItems(List<CreateOrderItemDto> itemDtos);

    @Mapping(target = "productId", source = "productId", qualifiedByName = "uuidToProductId")
    @Mapping(target = "price", source = "price", qualifiedByName = "bigDecimalToMoney")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "integerToQuantity")
    @Mapping(target = "totalPrice", source = "totalPrice", qualifiedByName = "bigDecimalToMoney")
    OrderItem toOrderItem(CreateOrderItemDto itemDto);

    OrderAddress toOrderAddress(CreateOrderAddressDto addressDto);

    @Mapping(target = "productId", source = "productId.productId")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "quantity", source = "quantity.value")
    @Mapping(target = "totalPrice", source = "totalPrice.amount")
    OrderItemDto toOrderItemDto(OrderItem item);

    List<OrderItemDto> toOrderItemDtos(List<OrderItem> items);

    OrderAddressDto toOrderAddressDto(OrderAddress orderAddress);

    @Named("uuidToProductId")
    default ProductId uuidToProductId(UUID uuid) {
        return new ProductId(uuid);
    }

    @Named("bigDecimalToMoney")
    default Money bigDecimalToMoney(BigDecimal amount) {
        return new Money(amount);
    }

    @Named("integerToQuantity")
    default Quantity integerToQuantity(Integer value) {
        return new Quantity(value);
    }
}