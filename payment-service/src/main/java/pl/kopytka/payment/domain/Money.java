package pl.kopytka.payment.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(BigDecimal amount) {
        this.amount = setScale(amount);
    }

    public boolean isGreaterThanZero() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isGreaterOrEqualThan(Money money) {
        return amount.compareTo(money.amount) >= 0;
    }

    public Money add(Money money) {
        return new Money(setScale(amount.add(money.amount)));
    }

    public Money subtract(Money money) {
        return new Money(setScale(amount.subtract(money.amount)));
    }

    public Money multiply(int multiplier) {
        return new Money(setScale(amount.multiply(BigDecimal.valueOf(multiplier))));
    }

    private static BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }
}
