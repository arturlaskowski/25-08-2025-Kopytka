package pl.kopytka.order.domain;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class OrderItemId {

    private Integer id;
    private Order order;
}
