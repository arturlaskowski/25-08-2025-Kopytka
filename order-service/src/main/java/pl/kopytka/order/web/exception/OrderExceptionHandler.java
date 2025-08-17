package pl.kopytka.order.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kopytka.order.application.exception.OrderNotFoundException;
import pl.kopytka.order.application.integration.customer.CustomerNotFoundException;
import pl.kopytka.order.application.integration.customer.CustomerServiceUnavailableException;
import pl.kopytka.order.application.integration.payment.PaymentProcessingFailedException;
import pl.kopytka.order.application.integration.payment.PaymentServiceUnavailableException;
import pl.kopytka.order.domain.OrderDomainException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("JvmTaintAnalysis")
public class OrderExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "Unexpected error!",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var aggregatedErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                aggregatedErrors,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ErrorResponse> handleOrderDomainException(OrderDomainException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {OrderNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleException(OrderNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CustomerNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CustomerServiceUnavailableException.class, PaymentServiceUnavailableException.class})
    public ResponseEntity<ErrorResponse> handleServiceUnavailableExceptions(Exception ex, HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "External service is temporarily unavailable",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {PaymentProcessingFailedException.class})
    public ResponseEntity<ErrorResponse> handlePaymentProcessingFailedException(PaymentProcessingFailedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
