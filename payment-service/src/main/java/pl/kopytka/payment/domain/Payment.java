package pl.kopytka.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;

import java.time.Instant;

import static pl.kopytka.payment.domain.PaymentStatus.*;

@Entity(name = "payments")
@Getter
public class Payment {

    @Id
    private PaymentId id;

    @AttributeOverride(name = "paymentId", column = @Column(name = "order_id"))
    private OrderId orderId;

    @AttributeOverride(name = "paymentId", column = @Column(name = "customer_id"))
    private CustomerId customerId;

    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "creation_date")
    private Instant creationDate;

    private String errorMessage;

    @Version
    private int version;

    protected Payment() {
    }

    public Payment(OrderId orderId, CustomerId customerId, Money price) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.price = price;
    }

    public void initialize() {
        if (id == null) {
            id = PaymentId.newOne();
        }
        creationDate = Instant.now();
    }

    public void validatePaymentPrice() {
        if (!price.isGreaterThanZero()) {
            throw new PaymentDomainException("Payment price must be greater than zero. Payment price: " + price.amount());
        }
    }

    public void complete() {
        if (COMPLETED == status || CANCELLED == status) {
            throw new PaymentDomainException("The complete operation cannot be performed. Payment is in incorrect state: " + status);
        }
        status = COMPLETED;
    }

    public void cancel() {
        if (CANCELLED == status) {
            throw new PaymentDomainException("Payment is already cancelled");
        }
        status = CANCELLED;
    }

    public void rejected(String reason) {
        errorMessage = reason;
        status = REJECTED;
    }

    public boolean isCompleted() {
        return status == COMPLETED;
    }

    public boolean isRejected() {
        return status == REJECTED;
    }

    public boolean isCanceled() {
        return status == CANCELLED;
    }
}
