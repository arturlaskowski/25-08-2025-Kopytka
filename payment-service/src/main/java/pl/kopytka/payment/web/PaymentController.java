package pl.kopytka.payment.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kopytka.common.web.dto.MakePaymentRequest;
import pl.kopytka.payment.application.PaymentApplicationService;
import pl.kopytka.payment.application.dto.CancelPaymentCommand;
import pl.kopytka.payment.application.dto.MakePaymentCommand;
import pl.kopytka.common.web.dto.PaymentResult;
import pl.kopytka.payment.web.dto.CancelPaymentRequest;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResult> makePayment(@Valid @RequestBody MakePaymentRequest request) {
        MakePaymentCommand command = new MakePaymentCommand(
                request.orderId(),
                request.customerId(),
                request.price()
        );

        var paymentResult = paymentApplicationService.makePayment(command);
        return ResponseEntity.ok(paymentResult);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@Valid @RequestBody CancelPaymentRequest request) {
        CancelPaymentCommand command = new CancelPaymentCommand(
                request.orderId(),
                request.customerId()
        );

        paymentApplicationService.cancelPayment(command);
        return ResponseEntity.ok().build();
    }
}
