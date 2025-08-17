package pl.kopytka.order.application.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.kopytka.order.domain.Order;
import pl.kopytka.order.domain.OrderAddress;
import pl.kopytka.order.domain.OrderItem;
import pl.kopytka.order.web.dto.GetOrderAddressDto;
import pl.kopytka.order.web.dto.GetOrderByIdQuery;
import pl.kopytka.order.web.dto.GetOrderItemDto;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OrderQueryMapper {

    public static GetOrderByIdQuery mapToGetOrderByIdQuery(Order order) {
        var itemsResponse = order.getItems().stream()
                .map(OrderQueryMapper::mapToItemDto)
                .toList();

        var addressDto = mapToAddressDto(order.getAddress());

        return new GetOrderByIdQuery(
                order.getId().id(),
                order.getCustomerId().id(),
                order.getPrice().amount(),
                order.getStatus(),
                itemsResponse,
                addressDto
        );
    }

    private static GetOrderItemDto mapToItemDto(OrderItem orderItem) {
        return new GetOrderItemDto(
                orderItem.getProductId(),
                orderItem.getQuantity().value(),
                orderItem.getPrice().amount(),
                orderItem.getTotalPrice().amount()
        );
    }

    private static GetOrderAddressDto mapToAddressDto(OrderAddress address) {
        return new GetOrderAddressDto(
                address.getStreet(),
                address.getPostCode(),
                address.getCity(),
                address.getHouseNo()
        );
    }
}
