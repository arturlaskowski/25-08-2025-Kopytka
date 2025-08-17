package pl.kopytka.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kopytka.common.domain.valueobject.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    private OrderId id;

    private CustomerId customerId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id", referencedColumnName = "id")
    private OrderAddress deliveryAddress;

    @NotNull
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull
    @Column(name = "creation_date")
    private Instant creationDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderItem> basket = new HashSet<>();

    @Column(name = "failure_messages", length = 2000)
    private String failureMessages;

    @Version
    private int version;

    public Order(CustomerId customerId, OrderAddress deliveryAddress, Money price, Set<OrderItem> basketItems) {
        this.id = OrderId.newOne();
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.price = price;
        this.status = OrderStatus.PENDING;
        this.creationDate = Instant.now();

        validatePrice(price);
        validateBasketItemsPrice(basketItems);
        validateTotalPrice(price, basketItems);

        initializeBasket(basketItems);
    }

    private void validatePrice(Money price) {
        if (!price.isGreaterThanZero()) {
            throw new OrderDomainException("Order price: " + price.amount() + " must be greater than zero");
        }
    }

    private void validateBasketItemsPrice(Set<OrderItem> basketItems) {
        basketItems.forEach(item -> {
            if (!item.isValidPrice()) {
                throw new OrderDomainException("Incorrect basket item price");
            }
        });
    }

    private void validateTotalPrice(Money price, Set<OrderItem> basketItems) {
        Money basketTotal = calculateTotalPrice(basketItems);
        if (!price.equals(basketTotal)) {
            throw new OrderDomainException("Total order price: " + price.amount() +
                    " is different than basket items total: " + basketTotal.amount());
        }
    }

    private Money calculateTotalPrice(Set<OrderItem> basketItems) {
        return basketItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money::add)
                .orElse(Money.ZERO);
    }

    private void initializeBasket(Set<OrderItem> basketItems) {
        int itemNumber = 1;
        for (OrderItem item : basketItems) {
            item.initializeBasketItem(this, itemNumber++);
            basket.add(item);
        }
    }

    public void pay() {
        if (!isPendingStatus()) {
            throw new OrderDomainException("The payment operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.PAID;
    }

    public void fail(String failureMessage) {
        if (!isPendingStatus()) {
            throw new OrderDomainException("The fail operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.FAILED;
        updateFailureMessages(failureMessage);
    }

    public void approve() {
        if (!isPaidStatus()) {
            throw new OrderDomainException("The approve operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.APPROVED;
    }

    public boolean isPendingStatus() {
        return status == OrderStatus.PENDING;
    }

    public boolean isPaidStatus() {
        return status == OrderStatus.PAID;
    }

    public boolean isApprovedStatus() {
        return status == OrderStatus.APPROVED;
    }

    private void updateFailureMessages(String message) {
        if (failureMessages == null) {
            failureMessages = message;
        } else if (message != null) {
            failureMessages += ";" + message;
        }
    }
}
