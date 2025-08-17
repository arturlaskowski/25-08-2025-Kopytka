package pl.kopytka.payment.web.dto;

import java.math.BigDecimal;

public record AddFundsRequest(
        BigDecimal amount
) {
}
