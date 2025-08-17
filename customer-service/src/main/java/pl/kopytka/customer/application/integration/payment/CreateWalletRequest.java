package pl.kopytka.customer.application.integration.payment;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateWalletRequest(
        @NotNull UUID customerId,
        BigDecimal initialBalance
) {
}
