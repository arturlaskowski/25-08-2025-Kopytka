package pl.kopytka.order.application.integration.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.kopytka.common.web.dto.MakePaymentRequest;
import pl.kopytka.common.web.dto.PaymentResult;

@FeignClient(name = "payment-service")
interface PaymentServiceFeignClient {

    @PostMapping("/api/payments/process")
    PaymentResult processPayment(@RequestBody MakePaymentRequest request);
}
