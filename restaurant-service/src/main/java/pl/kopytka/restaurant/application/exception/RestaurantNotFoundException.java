package pl.kopytka.restaurant.application.exception;

public class RestaurantNotFoundException extends RuntimeException {
    
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
