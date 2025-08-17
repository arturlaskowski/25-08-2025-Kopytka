package pl.kopytka.payment.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kopytka.common.web.ErrorResponse;
import pl.kopytka.common.web.GlobalExceptionHandler;
import pl.kopytka.payment.application.exception.PaymentAlreadyExistsException;
import pl.kopytka.payment.application.exception.PaymentNotFoundException;
import pl.kopytka.payment.application.exception.WalletNotFoundException;
import pl.kopytka.payment.domain.PaymentDomainException;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("JvmTaintAnalysis")
class PaymentExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(PaymentDomainException.class)
    public ResponseEntity<ErrorResponse> handlePaymentDomainException(PaymentDomainException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePaymentDomainException(PaymentAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(PaymentNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(WalletNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
