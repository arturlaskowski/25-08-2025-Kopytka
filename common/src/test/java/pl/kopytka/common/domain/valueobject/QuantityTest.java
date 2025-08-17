package pl.kopytka.common.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QuantityTest {

    @Test
    void shouldCreateQuantityWithValidValue() {
        //given
        int validQuantity = 5;

        //when
        Quantity quantity = new Quantity(validQuantity);

        //then
        assertThat(quantity.value()).isEqualTo(validQuantity);
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        //expected
        assertThatThrownBy(() -> new Quantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        //expected
        assertThatThrownBy(() -> new Quantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void shouldAllowCreationOfMinimumQuantity() {
        //given
        int minimumValidQuantity = 1;

        //when
        Quantity quantity = new Quantity(minimumValidQuantity);

        //then
        assertThat(quantity.value()).isEqualTo(minimumValidQuantity);
    }
}
