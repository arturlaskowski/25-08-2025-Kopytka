package pl.kopytka.trackorder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.kopytka.common.web.ErrorResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
class TrackOrderRestEndpoint {

    private final TrackingOrderQueryRepository trackingOrderQueryRepository;

    //Po dogadaniu z konsumentami należy zmienić na np. order-tracking/{id}
    @GetMapping("/api/orders/{orderId}/track")
    public TrackingOrderProjection trackOrder(@PathVariable UUID orderId) {
        return trackingOrderQueryRepository.findById(orderId)
                .orElseThrow(() -> new TrackingOrderNotFoundException(orderId));
    }

    @SuppressWarnings("JvmTaintAnalysis")
    @ExceptionHandler(value = TrackingOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(TrackingOrderNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
