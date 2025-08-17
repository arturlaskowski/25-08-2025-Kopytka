package pl.kopytka.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.payment.application.exception.WalletAlreadyExistsException;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.Wallet;
import pl.kopytka.payment.domain.WalletId;
import pl.kopytka.payment.web.dto.WalletDto;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public void createWallet(CustomerId customerId, Money initialAmount) {
        walletRepository.findByCustomerId(customerId).ifPresent(wallet -> {
            throw new WalletAlreadyExistsException(customerId);
        });

        WalletId walletId = WalletId.newOne();
        Wallet wallet = new Wallet(walletId, customerId, initialAmount);

       walletRepository.save(wallet);
    }

    @Transactional
    public void addFunds(WalletId walletId, Money amountToAdd) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        wallet.addCreditAmount(amountToAdd);
    }

    @Transactional(readOnly = true)
    public WalletDto getWallet(WalletId walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        return mapToDto(wallet);
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletByCustomerId(CustomerId customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new WalletNotFoundException(customerId));

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
