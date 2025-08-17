package pl.kopytka.payment.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kopytka.payment.application.PaymentApplicationService;
import pl.kopytka.payment.application.dto.CancelPaymentCommand;
import pl.kopytka.payment.application.dto.MakePaymentCommand;
import pl.kopytka.payment.web.dto.MakePaymentRequest;
import pl.kopytka.payment.web.dto.PaymentResultResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResultResponse> makePayment(@Valid @RequestBody MakePaymentRequest request) {
        MakePaymentCommand command = new MakePaymentCommand(
                request.orderId(),
                request.customerId(),
                request.price()
        );

        var paymentResult = paymentApplicationService.makePayment(command);
        return ResponseEntity.ok(paymentResult);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@RequestParam UUID paymentId, @RequestParam UUID customerId) {
        CancelPaymentCommand command = new CancelPaymentCommand(
                paymentId,
                customerId
        );

        paymentApplicationService.cancelPayment(command);
        return ResponseEntity.ok().build();
    }
}
