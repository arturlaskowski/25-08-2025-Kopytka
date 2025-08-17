package pl.kopytka.customer.application.integration.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.kopytka.common.web.dto.CreateWalletRequest;

@FeignClient(name = "payment-service")
public interface PaymentServiceFeignClient {
    
    @PostMapping("/api/wallets")
    void createWallet(@RequestBody CreateWalletRequest request);
}
