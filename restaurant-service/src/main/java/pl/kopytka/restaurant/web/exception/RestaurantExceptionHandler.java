package pl.kopytka.restaurant.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kopytka.common.web.ErrorResponse;
import pl.kopytka.common.web.GlobalExceptionHandler;
import pl.kopytka.restaurant.application.exception.RestaurantNotFoundException;
import pl.kopytka.restaurant.domain.exception.RestaurantDomainException;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("JvmTaintAnalysis")
class RestaurantExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(value = RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(RestaurantNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RestaurantDomainException.class)
    public ResponseEntity<ErrorResponse> handlePaymentDomainException(RestaurantDomainException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
