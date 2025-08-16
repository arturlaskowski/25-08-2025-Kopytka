package pl.kopytka.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void canCreateMoneyFromBigDecimal() {
        //expect
        assertEquals(new BigDecimal("10.00"), new Money(new BigDecimal("10.00")).amount());
        assertEquals(new BigDecimal("17.89"), new Money(new BigDecimal("17.89")).amount());
        assertEquals(new BigDecimal("0.00"), new Money(new BigDecimal("0.00")).amount());
    }

    @Test
    void shouldSetScale() {
        assertEquals(new BigDecimal("10.01"), new Money(new BigDecimal("10.006")).amount());
        assertEquals(new BigDecimal("10.00"), new Money(new BigDecimal("10.004")).amount());
        assertEquals(new BigDecimal("10.00"), new Money(new BigDecimal("10.005")).amount());
    }

    @Test
    void canAddMoney() {
        assertEquals(new Money(new BigDecimal("10.00")), new Money(new BigDecimal("3.00")).add(new Money(new BigDecimal("7.00"))));
        assertEquals(new Money(new BigDecimal("35.41")), new Money(new BigDecimal("18.94")).add(new Money(new BigDecimal("16.47"))));
        assertEquals(new Money(new BigDecimal("7.00")), new Money(new BigDecimal("0.00")).add(new Money(new BigDecimal("7.00"))));
    }

    @Test
    void canSubtractMoney() {
        assertEquals(new Money(new BigDecimal("30.00")), new Money(new BigDecimal("50.00")).subtract(new Money(new BigDecimal("20.00"))));
        assertEquals(Money.ZERO, new Money(new BigDecimal("50.00")).subtract(new Money(new BigDecimal("50.00"))));
        assertEquals(new Money(new BigDecimal("27.04")), new Money(new BigDecimal("56.36")).subtract(new Money(new BigDecimal("29.32"))));
    }

    @Test
    void canMultiplyMoney() {
        assertEquals(new Money(new BigDecimal("9.00")), new Money(new BigDecimal("3.00")).multiply(3));
        assertEquals(new Money(new BigDecimal("8.65")), new Money(new BigDecimal("8.65")).multiply(1));
        assertEquals(new Money(new BigDecimal("65.28")), new Money(new BigDecimal("16.32")).multiply(4));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Money(null));
        assertEquals("Amount cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        var negativeAmount = new BigDecimal(-1);
        var exception = assertThrows(IllegalArgumentException.class, () -> new Money(negativeAmount));
        assertEquals("Amount cannot be negative", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMultiplierIsZero() {
        var money = new Money(new BigDecimal("10.00"));
        var multiplier = 0;
        var exception = assertThrows(IllegalArgumentException.class, () -> money.multiply(multiplier));
        assertEquals("Multiplier must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMultiplierIsNegative() {
        var money = new Money(new BigDecimal("10.00"));
        var multiplier = -1;
        var exception = assertThrows(IllegalArgumentException.class, () -> money.multiply(multiplier));
        assertEquals("Multiplier must be greater than zero", exception.getMessage());
    }
}