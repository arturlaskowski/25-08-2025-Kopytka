package pl.kopytka.customer.application.integration.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceFeignClient {
    
    @PostMapping("/api/wallets")
    void createWallet(@RequestBody CreateWalletRequest request);
}
