package pl.kopytka.order.saga;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kopytka.common.saga.SagaStatus;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "order_sagas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class OrderSaga {

    @Id
    private UUID id;

    @Column(unique = true)
    private UUID orderId;

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 2000)
    private String errorMessage;

    @Version
    private int version;

    public static OrderSaga create(UUID orderId, UUID customerId) {
        OrderSaga saga = new OrderSaga();
        saga.id = UUID.randomUUID();
        saga.orderId = orderId;
        saga.customerId = customerId;
        saga.status = SagaStatus.PROCESSING;
        saga.createdAt = Instant.now();
        saga.updatedAt = Instant.now();
        return saga;
    }

    public void processing() {
        this.status = SagaStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void failed(String errorMessage) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    public void compensated(String errorMessage) {
        this.status = SagaStatus.COMPENSATED;
        this.updatedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void compensating(String errorMessage) {
        this.status = SagaStatus.COMPENSATING;
        this.updatedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void complete() {
        this.status = SagaStatus.SUCCEEDED;
        this.updatedAt = Instant.now();
    }

}