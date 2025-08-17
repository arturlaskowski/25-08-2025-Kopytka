package pl.kopytka.payment.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletRequest(
        UUID customerId,
        BigDecimal initialAmount
) {
}
