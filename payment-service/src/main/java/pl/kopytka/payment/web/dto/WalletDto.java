package pl.kopytka.payment.web.dto;

import java.math.BigDecimal;

public record WalletDto(
        String id,
        String customerId,
        BigDecimal balance
) {
}
