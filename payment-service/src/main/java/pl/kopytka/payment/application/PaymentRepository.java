package pl.kopytka.payment.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.payment.domain.Payment;
import pl.kopytka.payment.domain.PaymentId;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

    @Query("SELECT p FROM payments p WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") OrderId orderId);

}
