package pl.kopytka.order.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.kopytka.common.ErrorResponse;
import pl.kopytka.order.application.exception.InvalidOrderException;
import pl.kopytka.order.application.exception.OrderNotFoundException;
import pl.kopytka.order.domain.OrderDomainException;

@ControllerAdvice
public class OrderRestApiExceptionHandler {

    @ExceptionHandler(value = InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleException(InvalidOrderException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(OrderNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = OrderDomainException.class)
    public ResponseEntity<ErrorResponse> handleException(OrderDomainException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}

