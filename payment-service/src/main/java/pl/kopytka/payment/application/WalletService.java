package pl.kopytka.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.payment.application.exception.WalletAlreadyExistsException;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.CustomerId;
import pl.kopytka.payment.domain.Money;
import pl.kopytka.payment.domain.Wallet;
import pl.kopytka.payment.domain.WalletId;
import pl.kopytka.payment.web.dto.CreateWalletRequest;
import pl.kopytka.payment.web.dto.WalletDto;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public WalletId createWallet(CreateWalletRequest request) {
        CustomerId customerId = new CustomerId(request.customerId());
        Money initialAmount = request.initialAmount() != null ? new Money(request.initialAmount()) : Money.ZERO;

        walletRepository.findByCustomerId(customerId).ifPresent(wallet -> {
            throw new WalletAlreadyExistsException(customerId);
        });

        WalletId walletId = WalletId.newOne();
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

        Wallet savedWallet = walletRepository.save(wallet);
        return savedWallet.getId();
    }

    @Transactional
    public void addFunds(WalletId walletId, Money amountToAdd) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with ID " + walletId.id() + " not found"));

        wallet.addCreditAmount(amountToAdd);
    }

    @Transactional(readOnly = true)
    public WalletDto getWallet(WalletId walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with ID " + walletId.id() + " not found"));

        return mapToDto(wallet);
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletByCustomerId(CustomerId customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet for customer ID " + customerId.id() + " not found"));

        return mapToDto(wallet);
    }

    private WalletDto mapToDto(Wallet wallet) {
        return new WalletDto(
                wallet.getId().id().toString(),
                wallet.getCustomerId().id().toString(),
                wallet.getAmount().amount()
        );
    }
}
