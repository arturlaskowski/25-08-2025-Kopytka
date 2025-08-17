package pl.kopytka.payment.domain.entity;

import org.junit.jupiter.api.Test;
import pl.kopytka.payment.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldCreatePaymentWithValidData() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));

        //when
        Payment payment = new Payment(orderId, customerId, price);

        //then
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getCustomerId()).isEqualTo(customerId);
        assertThat(payment.getPrice()).isEqualTo(price);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getStatus()).isNull();
    }

    @Test
    void shouldInitializePaymentCorrectly() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        
        Instant beforeInitialization = Instant.now();

        //when
        payment.initialize();
        
        Instant afterInitialization = Instant.now();

        //then
        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getCreationDate())
                .isNotNull()
                .isAfterOrEqualTo(beforeInitialization)
                .isBeforeOrEqualTo(afterInitialization);
    }

    @Test
    void shouldValidatePositivePaymentPriceSuccessfully() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money positivePrice = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, positivePrice);

        //expected
        assertThatNoException().isThrownBy(payment::validatePaymentPrice);
    }

    @Test
    void shouldThrowExceptionWhenValidatingZeroPaymentPrice() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money zeroPrice = new Money(BigDecimal.ZERO);
        Payment payment = new Payment(orderId, customerId, zeroPrice);

        //expected
        assertThatThrownBy(payment::validatePaymentPrice)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Payment price must be greater than zero");
    }

    @Test
    void shouldThrowExceptionWhenValidatingNegativePaymentPrice() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money negativePrice = new Money(new BigDecimal("-1.00"));
        Payment payment = new Payment(orderId, customerId, negativePrice);

        //expected
        assertThatThrownBy(payment::validatePaymentPrice)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Payment price must be greater than zero");
    }

    @Test
    void shouldCompletePaymentSuccessfully() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize(); // Initialize to set the id

        //when
        payment.complete();

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.isCompleted()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenCompletingAlreadyCompletedPayment() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize();
        payment.complete(); // First completion

        //expected
        assertThatThrownBy(payment::complete)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("The complete operation cannot be performed. Payment is in incorrect state");
    }

    @Test
    void shouldThrowExceptionWhenCompletingCancelledPayment() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize();
        payment.cancel(); // Cancel the payment

        //expected
        assertThatThrownBy(payment::complete)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("The complete operation cannot be performed. Payment is in incorrect state");
    }

    @Test
    void shouldCancelPaymentSuccessfully() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize(); // Initialize to set the id

        //when
        payment.cancel();

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.isCanceled()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledPayment() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize();
        payment.cancel(); // First cancellation

        //expected
        assertThatThrownBy(payment::cancel)
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Payment is already cancelled");
    }

    @Test
    void shouldRejectPaymentSuccessfully() {
        //given
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money price = new Money(new BigDecimal("50.00"));
        Payment payment = new Payment(orderId, customerId, price);
        payment.initialize(); // Initialize to set the id

        //when
        payment.rejected("Test rejection reason");

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.isRejected()).isTrue();
    }
}
