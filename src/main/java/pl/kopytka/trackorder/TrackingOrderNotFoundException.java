package pl.kopytka.trackorder;


import java.util.UUID;

class TrackingOrderNotFoundException extends RuntimeException {

    public TrackingOrderNotFoundException(UUID orderId) {
        super(String.format("Could not find order with orderId:  %s", orderId));
    }
}
