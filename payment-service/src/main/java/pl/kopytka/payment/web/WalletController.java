package pl.kopytka.payment.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.payment.application.WalletService;
import pl.kopytka.payment.domain.WalletId;
import pl.kopytka.payment.web.dto.AddFundsRequest;
import pl.kopytka.payment.web.dto.WalletDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
class WalletController {

    private final WalletService walletService;

    @PostMapping("/{walletId}/funds")
    public ResponseEntity<Void> addFunds(
            @PathVariable UUID walletId,
            @Valid @RequestBody AddFundsRequest request) {
        walletService.addFunds(new WalletId(walletId), new Money(request.amount()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWalletById(@PathVariable UUID walletId) {
        WalletDto wallet = walletService.getWallet(new WalletId(walletId));
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<WalletDto> getWalletByCustomerId(@PathVariable UUID customerId) {
        WalletDto wallet = walletService.getWalletByCustomerId(new CustomerId(customerId));
        return ResponseEntity.ok(wallet);
    }
}
