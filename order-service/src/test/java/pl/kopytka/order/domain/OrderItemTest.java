package pl.kopytka.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    void shouldCreateBasketItemWithValidData() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("15.50"));
        Quantity quantity = new Quantity(3);
        Integer itemNumber = 1;

        //when
        OrderItem basketItem = new OrderItem(productId, price, quantity, itemNumber);

        //then
        assertThat(basketItem.getProductId()).isEqualTo(productId);
        assertThat(basketItem.getPrice()).isEqualTo(price);
        assertThat(basketItem.getQuantity()).isEqualTo(quantity);
        assertThat(basketItem.getId()).isEqualTo(itemNumber);
        assertThat(basketItem.getTotalPrice()).isEqualTo(new Money(new BigDecimal("46.50")));
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("10.00"));
        Quantity quantity = new Quantity(5);
        Integer itemNumber = 1;
        Money expectedTotalPrice = new Money(new BigDecimal("50.00"));

        //when
        OrderItem basketItem = new OrderItem(productId, price, quantity, itemNumber);

        //then
        assertThat(basketItem.getTotalPrice()).isEqualTo(expectedTotalPrice);
    }

    @Test
    void shouldValidatePriceCorrectlyWhenPriceIsPositive() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("10.00"));
        Quantity quantity = new Quantity(2);
        Money totalPrice = new Money(new BigDecimal("20.00"));
        Integer itemNumber = 1;
        OrderItem basketItem = new OrderItem(productId, price, quantity, totalPrice, itemNumber);

        //when
        boolean isValid = basketItem.isValidPrice();

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidatePriceWhenPriceIsZero() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(BigDecimal.ZERO);
        Quantity quantity = new Quantity(2);
        Money totalPrice = new Money(BigDecimal.ZERO);
        Integer itemNumber = 1;
        OrderItem basketItem = new OrderItem(productId, price, quantity, totalPrice, itemNumber);

        //when
        boolean isValid = basketItem.isValidPrice();

        //then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidatePriceWhenTotalPriceDoesNotMatchCalculation() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("10.00"));
        Quantity quantity = new Quantity(2);
        Money incorrectTotalPrice = new Money(new BigDecimal("25.00")); // Should be 20.00
        Integer itemNumber = 1;

        //when & then
        assertThatThrownBy(() -> new OrderItem(productId, price, quantity, incorrectTotalPrice, itemNumber))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("Total price should be equal to price multiplied by quantity");
    }

    @Test
    void shouldCreateBasketItemWithCalculatedTotalPrice() {
        //given
        ProductId productId = new ProductId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("10.00"));
        Quantity quantity = new Quantity(2);
        Integer itemNumber = 1;
        Money expectedTotalPrice = new Money(new BigDecimal("20.00"));

        //when
        OrderItem basketItem = new OrderItem(productId, price, quantity, itemNumber);

        //then
        assertThat(basketItem.getTotalPrice()).isEqualTo(expectedTotalPrice);
    }
}
