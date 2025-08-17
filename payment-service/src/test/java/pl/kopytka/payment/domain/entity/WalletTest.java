package pl.kopytka.payment.domain.entity;

import org.junit.jupiter.api.Test;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.payment.domain.PaymentDomainException;
import pl.kopytka.payment.domain.Wallet;
import pl.kopytka.payment.domain.WalletId;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    @Test
    void shouldCreateWalletWithValidData() {
        //given
        WalletId walletId = WalletId.newOne();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money initialAmount = new Money(new BigDecimal("100.00"));

        //when
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        //then
        assertThat(wallet.getId()).isEqualTo(walletId);
        assertThat(wallet.getCustomerId()).isEqualTo(customerId);
        assertThat(wallet.getAmount()).isEqualTo(initialAmount);
    }

    @Test
    void shouldSubtractCreditAmountSuccessfully() {
        //given
        WalletId walletId = WalletId.newOne();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money initialAmount = new Money(new BigDecimal("100.00"));
        Money amountToSubtract = new Money(new BigDecimal("50.00"));
        Money expectedRemainingAmount = new Money(new BigDecimal("50.00"));
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        //when
        wallet.subtractCreditAmount(amountToSubtract);

        //then
        assertThat(wallet.getAmount()).isEqualTo(expectedRemainingAmount);
    }

    @Test
    void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
        //given
        WalletId walletId = WalletId.newOne();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money initialAmount = new Money(new BigDecimal("50.00"));
        Money amountToSubtract = new Money(new BigDecimal("75.00"));
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        //expected
        assertThatThrownBy(() -> wallet.subtractCreditAmount(amountToSubtract))
                .isInstanceOf(PaymentDomainException.class)
                .hasMessageContaining("Insufficient funds in wallet");
    }

    @Test
    void shouldAddCreditAmountSuccessfully() {
        //given
        WalletId walletId = WalletId.newOne();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money initialAmount = new Money(new BigDecimal("100.00"));
        Money amountToAdd = new Money(new BigDecimal("25.00"));
        Money expectedFinalAmount = new Money(new BigDecimal("125.00"));
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        //when
        wallet.addCreditAmount(amountToAdd);

        //then
        assertThat(wallet.getAmount()).isEqualTo(expectedFinalAmount);
    }

    @Test
    void shouldAllowSubtractingExactAvailableAmount() {
        //given
        WalletId walletId = WalletId.newOne();
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Money initialAmount = new Money(new BigDecimal("100.00"));
        Money amountToSubtract = new Money(new BigDecimal("100.00"));
        Money expectedRemainingAmount = new Money(BigDecimal.ZERO);
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        //when
        wallet.subtractCreditAmount(amountToSubtract);

        //then
        assertThat(wallet.getAmount()).isEqualTo(expectedRemainingAmount);
    }
}
