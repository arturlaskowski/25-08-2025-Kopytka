package pl.kopytka.common.outbox.exception;

public class OutboxDeserializationException extends RuntimeException {

    public OutboxDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
