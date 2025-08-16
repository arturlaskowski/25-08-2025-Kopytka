package pl.kopytka.common;

import java.time.Instant;

public record ErrorResponse(
        String message,
        Instant timestamp,
        String path
) {

    public ErrorResponse(String message, String path) {
        this(message, Instant.now(), path);
    }
}