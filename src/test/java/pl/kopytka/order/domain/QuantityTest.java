package pl.kopytka.order.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityTest {

    @Test
    void canCreateQuantityFromPositiveInteger() {
        assertEquals(5, new Quantity(5).value());
        assertEquals(647, new Quantity(647).value());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Quantity(null));
        assertEquals("Number of elements cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenValueIsZero() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Quantity(0));
        assertEquals("Number of elements must be at least 1", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNegative() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Quantity(-1));
        assertEquals("Number of elements must be at least 1", exception.getMessage());
    }
}