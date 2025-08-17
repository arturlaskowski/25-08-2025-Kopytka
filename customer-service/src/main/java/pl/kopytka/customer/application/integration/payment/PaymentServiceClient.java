package pl.kopytka.customer.application.integration.payment;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.web.ServiceUnavailableException;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final PaymentServiceFeignClient paymentServiceFeignClient;

    public void createWallet(CustomerId customerId) {
        try {
            CreateWalletRequest request = new CreateWalletRequest(
                    customerId.id(),
                    BigDecimal.ZERO
            );

            paymentServiceFeignClient.createWallet(request);
            log.info("Successfully created wallet for customer: {}", customerId.id());

        } catch (FeignException e) {
            log.error("Error creating wallet for customer: {}, status: {}, response: {}",
                    customerId.id(), e.status(), e.contentUTF8(), e);
            throw new ServiceUnavailableException("Payment", "creating wallet", e);

        } catch (Exception e) {
            log.error("Unexpected error creating wallet for customer: {}", customerId.id(), e);
            throw new ServiceUnavailableException("Payment", "creating wallet", e);
        }
    }
}