package pl.kopytka.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pl.kopytka.common.domain.CustomerId;
import pl.kopytka.common.domain.Money;
import pl.kopytka.common.domain.OrderId;

import java.time.Instant;
import java.util.List;

import static pl.kopytka.order.domain.OrderStatus.PAID;
import static pl.kopytka.order.domain.OrderStatus.PENDING;


@Entity(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    private OrderId id;

    @NotNull
    private Instant createAt;

    @NotNull
    private Instant lastUpdateAt;

    @NotNull
    private CustomerId customerId;

    @NotNull
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private OrderAddress address;

    @NotNull
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private List<OrderItem> items;

    @Version
    private int version;

    public Order(OrderId orderId, CustomerId customerId, Money price, List<OrderItem> items, OrderAddress address) {
        this.id = orderId;
        this.customerId = customerId;
        this.price = price;
        this.items = items;
        this.address = address;
        initialize();
    }

    private void initialize() {
        this.createAt = Instant.now();
        this.lastUpdateAt = Instant.now();
        this.status = PENDING;
        validatePrice();
        initializeBasketItems();
    }

    private void validatePrice() {
        Money itemsTotalCost = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.ZERO, Money::add);

        if (!price.equals(itemsTotalCost)) {
            throw new OrderDomainException("Total order price: " + price
                    + " is different than order basketItems total: " + itemsTotalCost);
        }
    }

    private void initializeBasketItems() {
        int itemNumber = 1;
        for (OrderItem item : items) {
            item.initializeBasketItem(this, itemNumber++);
        }
    }

    public void pay() {
        if (PENDING != status) {
            throw new OrderDomainException("Order is not in correct state for pay operation");
        }
        lastUpdateAt = Instant.now();
        status = PAID;
    }

    public void approve() {
        if (!isPaidStatus()) {
            throw new OrderDomainException("The approve operation cannot be performed. Order is in incorrect state");
        }
        this.status = OrderStatus.APPROVED;
    }

    public boolean isPendingStatus() {
        return PENDING == status;
    }

    public boolean isPaidStatus() {
        return PAID == status;
    }

}
