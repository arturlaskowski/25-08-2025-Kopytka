package pl.kopytka.order.application.integration.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceFeignClient {

    @PostMapping("/api/payments/process")
    PaymentResult processPayment(@RequestBody MakePaymentRequest request);
}
