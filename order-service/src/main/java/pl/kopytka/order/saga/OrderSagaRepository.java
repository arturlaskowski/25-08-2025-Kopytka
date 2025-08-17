package pl.kopytka.order.saga;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface OrderSagaRepository extends CrudRepository<OrderSaga, UUID> {

    Optional<OrderSaga> findByOrderId(UUID id);

    @Transactional
    @Modifying
    @Query("UPDATE order_sagas s SET s.status = 'FAILED', s.errorMessage = 'Saga timeout after 10 minutes', s.updatedAt = :currentTime " +
            "WHERE s.status IN ('PROCESSING', 'COMPENSATING') AND s.updatedAt < :cutoffTime")
    int markStaleProcessingSagasAsFailed(@Param("cutoffTime") Instant cutoffTime, @Param("currentTime") Instant currentTime);
}

