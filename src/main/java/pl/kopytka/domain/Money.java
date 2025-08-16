package pl.kopytka.domain;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = setScale(amount);
    }

    public Money add(Money money) {
        return new Money(this.amount.add(money.amount()));
    }

    public Money subtract(Money money) {
        return new Money(this.amount.subtract(money.amount));
    }

    public Money multiply(int multiplier) {
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be greater than zero");
        }
        return new Money(this.amount.multiply(new BigDecimal(multiplier)));
    }

    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }
}
