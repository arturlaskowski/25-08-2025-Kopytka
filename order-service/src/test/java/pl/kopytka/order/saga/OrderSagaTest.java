package pl.kopytka.order.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kopytka.common.saga.SagaStatus;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSagaTest {

    @Test
    @DisplayName("Should create OrderSaga with valid parameters")
    void shouldCreateOrderSagaWithValidParameters() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Instant beforeCreation = Instant.now();

        // when
        OrderSaga saga = OrderSaga.create(orderId, customerId);

        // then
        Instant afterCreation = Instant.now();
        assertThat(saga)
                .satisfies(s -> {
                    assertThat(s.getId()).isNotNull();
                    assertThat(s.getOrderId()).isEqualTo(orderId);
                    assertThat(s.getCustomerId()).isEqualTo(customerId);
                    assertThat(s.getStatus()).isEqualTo(SagaStatus.PROCESSING);
                    assertThat(s.getCreatedAt())
                            .isAfterOrEqualTo(beforeCreation)
                            .isBeforeOrEqualTo(afterCreation);
                    assertThat(s.getUpdatedAt())
                            .isAfterOrEqualTo(beforeCreation)
                            .isBeforeOrEqualTo(afterCreation);
                    assertThat(s.getErrorMessage()).isNull();
                    assertThat(s.getVersion()).isZero();
                });
    }

    @Test
    @DisplayName("Should transition to processing status")
    void shouldTransitionToProcessingStatus() {
        // given
        OrderSaga saga = createTestSaga();
        Instant beforeUpdate = Instant.now();

        // when
        saga.processing();

        // then
        Instant afterUpdate = Instant.now();

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        assertThat(saga.getUpdatedAt())
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("Should transition to failed status with error message")
    void shouldTransitionToFailedStatusWithErrorMessage() {
        // given
        OrderSaga saga = createTestSaga();
        String errorMessage = "Payment failed: insufficient funds";
        Instant beforeUpdate = Instant.now();

        // when
        saga.failed(errorMessage);

        // then
        Instant afterUpdate = Instant.now();

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(saga.getErrorMessage())
                .isNotNull()
                .isEqualTo(errorMessage);
        assertThat(saga.getUpdatedAt())
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("Should transition to compensating status with error message")
    void shouldTransitionToCompensatingStatusWithErrorMessage() {
        // given
        OrderSaga saga = createTestSaga();
        String errorMessage = "Restaurant rejected order";
        Instant beforeUpdate = Instant.now();

        // when
        saga.compensating(errorMessage);

        // then
        Instant afterUpdate = Instant.now();

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(saga.getErrorMessage())
                .isNotNull()
                .isEqualTo(errorMessage);
        assertThat(saga.getUpdatedAt())
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("Should transition to compensated status with error message")
    void shouldTransitionToCompensatedStatusWithErrorMessage() {
        // given
        OrderSaga saga = createTestSaga();
        String errorMessage = "Order compensation completed";
        Instant beforeUpdate = Instant.now();

        // when
        saga.compensated(errorMessage);

        // then
        Instant afterUpdate = Instant.now();

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(saga.getErrorMessage())
                .isNotNull()
                .isEqualTo(errorMessage);
        assertThat(saga.getUpdatedAt())
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("Should transition to succeeded status")
    void shouldTransitionToSucceededStatus() {
        // given
        OrderSaga saga = createTestSaga();
        Instant beforeUpdate = Instant.now();

        // when
        saga.complete();

        // then
        Instant afterUpdate = Instant.now();

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
        assertThat(saga.getUpdatedAt())
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    private OrderSaga createTestSaga() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        return OrderSaga.create(orderId, customerId);
    }
}
