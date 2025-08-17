package pl.kopytka.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    private WalletId id;

    @AttributeOverride(name = "customerId", column = @Column(name = "customer_id", unique = true))
    private CustomerId customerId;

    @AttributeOverride(name = "amount", column = @Column(name = "amount"))
    private Money amount;

    @Version
    private int version;

    public Wallet(WalletId id, CustomerId customerId, Money amount) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
    }

    public void subtractCreditAmount(Money money) {
        if (amount.amount().compareTo(money.amount()) < 0) {
            throw new PaymentDomainException("Insufficient funds in wallet");
        }
        amount = amount.subtract(money);
    }

    public void addCreditAmount(Money money) {
        amount = amount.add(money);
    }
}
