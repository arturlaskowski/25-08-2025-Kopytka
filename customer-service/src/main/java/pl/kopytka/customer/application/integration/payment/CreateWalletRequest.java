package pl.kopytka.customer.application.integration.payment;

import java.math.BigDecimal;

public record CreateWalletRequest(
        String customerId,
        BigDecimal initialAmount
) {
}
