package pl.kopytka.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.ProductId;
import pl.kopytka.common.domain.valueobject.Quantity;


@IdClass(OrderItemId.class)
@Entity(name = "order_items")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    private Integer id;

    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private ProductId productId;

    @NotNull
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @NotNull
    @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    private Quantity quantity;

    @NotNull
    @AttributeOverride(name = "amount", column = @Column(name = "total_price"))
    private Money totalPrice;

    public OrderItem(ProductId productId, Money price, Quantity quantity, Money totalPrice) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        validatePrice();
    }

    public OrderItem(ProductId productId, Money price, Quantity quantity, Money totalPrice, Integer itemNumber) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.id = itemNumber;
        validatePrice();
    }

    public OrderItem(ProductId productId, Money price, Quantity quantity, Integer itemNumber) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price.multiply(quantity.value());
        this.id = itemNumber;
        validatePrice();
    }

    private void validatePrice() {
        if (!price.multiply(quantity.value()).equals(totalPrice)) {
            throw new OrderDomainException("Total price should be equal to price multiplied by quantity. Expected: " +
                    price.multiply(quantity.value()) + " but was: " + totalPrice);
        }
    }

    void initializeBasketItem(Order order, Integer itemNumber) {
        this.order = order;
        this.id = itemNumber;
    }

    public boolean isValidPrice() {
        return price.isGreaterThanZero() &&
               price.multiply(quantity.value()).equals(totalPrice);
    }
}