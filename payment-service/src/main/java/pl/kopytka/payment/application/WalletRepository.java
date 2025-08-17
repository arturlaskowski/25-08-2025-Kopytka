package pl.kopytka.payment.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kopytka.payment.domain.CustomerId;
import pl.kopytka.payment.domain.Wallet;
import pl.kopytka.payment.domain.WalletId;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, WalletId> {

    Optional<Wallet> findByCustomerId(CustomerId customerId);
}

