package pl.kopytka.restaurant.messaging.exception;

public class RestaurantMessagingException extends RuntimeException {
    
    public RestaurantMessagingException(String message) {
        super(message);
    }
    
    public RestaurantMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
