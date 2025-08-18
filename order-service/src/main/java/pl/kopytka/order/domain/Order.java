package pl.kopytka.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.domain.valueobject.RestaurantId;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    private OrderId id;

    private CustomerId customerId;

    private RestaurantId restaurantId;

    @OneToOne(cascade = ALL)
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

    @OneToMany(mappedBy = "order", cascade = ALL, fetch = EAGER)
    private Set<OrderItem> basket = new HashSet<>();

    @Column(name = "failure_messages", length = 2000)
    private String failureMessages;

    @Version
    private int version;

    public Order(CustomerId customerId, RestaurantId restaurantId, OrderAddress deliveryAddress, Money price, Set<OrderItem> basketItems) {
        this.id = OrderId.newOne();
        this.restaurantId = restaurantId;
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
                throw new OrderDomainException("Total price should be equal to price multiplied by quantity");
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
        if (this.status != OrderStatus.PENDING) {
            throw new OrderDomainException("The payment operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.PAID;
    }

    public void approve() {
        if (this.status != OrderStatus.PAID) {
            throw new OrderDomainException("The approve operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.APPROVED;
    }

    public void initCancel(String failureMessages) {
        if (this.status != OrderStatus.PAID) {
            throw new OrderDomainException("Order must be in PAID status to be initiated for cancellation");
        }
        this.status = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    public void cancel(String failureMessages) {
        if (!(this.status == OrderStatus.CANCELLING || this.status == OrderStatus.PENDING)) {
            throw new OrderDomainException("Order must be in CANCELLING or PENDING status to be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
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
