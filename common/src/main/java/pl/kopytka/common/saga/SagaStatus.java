package pl.kopytka.common.saga;

public enum SagaStatus {
    PROCESSING,
    SUCCEEDED,
    COMPENSATING,
    COMPENSATED,
    FAILED,
}