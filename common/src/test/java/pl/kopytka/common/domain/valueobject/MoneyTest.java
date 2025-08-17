package pl.kopytka.common.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithCorrectScale() {
        //given
        BigDecimal amount = new BigDecimal("10.123");
        BigDecimal expectedAmount = new BigDecimal("10.12").setScale(2, RoundingMode.HALF_EVEN);

        //when
        Money money = new Money(amount);

        //then
        assertThat(money.amount()).isEqualTo(expectedAmount);
    }

    @Test
    void shouldRoundHalfEvenCorrectly() {
        //given
        BigDecimal amount1 = new BigDecimal("10.125");
        BigDecimal amount2 = new BigDecimal("10.135");
        BigDecimal expected1 = new BigDecimal("10.12").setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expected2 = new BigDecimal("10.14").setScale(2, RoundingMode.HALF_EVEN);

        //when
        Money money1 = new Money(amount1);
        Money money2 = new Money(amount2);

        //then
        assertThat(money1.amount()).isEqualTo(expected1);
        assertThat(money2.amount()).isEqualTo(expected2);
    }

    @Test
    void shouldDetectAmountGreaterThanZero() {
        //given
        Money positiveAmount = new Money(new BigDecimal("0.01"));
        Money zeroAmount = new Money(BigDecimal.ZERO);
        Money negativeAmount = new Money(new BigDecimal("-0.01"));

        //then
        assertThat(positiveAmount.isGreaterThanZero()).isTrue();
        assertThat(zeroAmount.isGreaterThanZero()).isFalse();
        assertThat(negativeAmount.isGreaterThanZero()).isFalse();
    }

    @Test
    void shouldCompareAmountsCorrectly() {
        //given
        Money larger = new Money(new BigDecimal("15.00"));
        Money smaller = new Money(new BigDecimal("10.00"));
        Money equal = new Money(new BigDecimal("15.00"));

        //then
        assertThat(larger.isGreaterOrEqualThan(smaller)).isTrue();
        assertThat(equal.isGreaterOrEqualThan(larger)).isTrue();
        assertThat(smaller.isGreaterOrEqualThan(larger)).isFalse();
    }

    @Test
    void shouldAddMoneyCorrectly() {
        //given
        Money first = new Money(new BigDecimal("10.50"));
        Money second = new Money(new BigDecimal("5.25"));
        Money expected = new Money(new BigDecimal("15.75"));

        //when
        Money result = first.add(second);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldSubtractMoneyCorrectly() {
        //given
        Money first = new Money(new BigDecimal("15.75"));
        Money second = new Money(new BigDecimal("5.25"));
        Money expected = new Money(new BigDecimal("10.50"));

        //when
        Money result = first.subtract(second);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldMultiplyMoneyCorrectly() {
        //given
        Money money = new Money(new BigDecimal("10.50"));
        int multiplier = 3;
        Money expected = new Money(new BigDecimal("31.50"));

        //when
        Money result = money.multiply(multiplier);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldDefineZeroConstant() {
        //given
        Money zero = Money.ZERO;
        Money expectedZero = new Money(BigDecimal.ZERO);

        //then
        assertThat(zero).isEqualTo(expectedZero);
        assertThat(zero.amount()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
    }
}
