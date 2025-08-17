package pl.kopytka.payment.domain;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record WalletId(UUID walletId) {

    public static WalletId newOne() {
        return new WalletId(UUID.randomUUID());
    }

    public UUID id() {
        return walletId;
    }
}
