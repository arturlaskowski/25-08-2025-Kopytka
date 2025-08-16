package pl.kopytka.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.kopytka.application.exception.CustomerAlreadyExistsException;
import pl.kopytka.application.exception.CustomerNotFoundException;
import pl.kopytka.application.exception.OrderNotFoundException;
import pl.kopytka.domain.OrderDomainException;

import java.util.stream.Collectors;

@ControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(value = CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(CustomerNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
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

    @ExceptionHandler(value = CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleException(CustomerAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String aggregatedErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(aggregatedErrors, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

