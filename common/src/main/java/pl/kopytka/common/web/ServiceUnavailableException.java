package pl.kopytka.common.web;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String serviceName, String operation, Throwable cause) {
        super(String.format("%s service is unavailable while %s", serviceName, operation), cause);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
