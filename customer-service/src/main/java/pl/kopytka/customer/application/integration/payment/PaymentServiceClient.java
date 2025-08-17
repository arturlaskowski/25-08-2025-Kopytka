package pl.kopytka.customer.application.integration.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.web.ServiceUnavailableException;
import pl.kopytka.common.web.dto.CreateWalletRequest;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;


    public void createWallet(CustomerId customerId) {
        try {
            String url = paymentServiceUrl + "/api/wallets";
            log.info("Creating wallet for customer: {}", customerId.id());

            CreateWalletRequest request = new CreateWalletRequest(
                    customerId.id(),
                    BigDecimal.ZERO // Initial amount is zero
            );

            restTemplate.postForEntity(url, request, Void.class);

            // If we get here, the request was successful (2xx)
            log.info("Successfully created wallet for customer: {}", customerId.id());

        } catch (HttpClientErrorException e) {
            log.error("Error creating wallet for customer: {}, status: {}, response: {}",
                    customerId.id(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ServiceUnavailableException("Payment", "creating wallet", e);

        } catch (Exception e) {
            log.error("Unexpected error creating wallet for customer: {}", customerId.id(), e);
            throw new ServiceUnavailableException("Payment", "creating wallet", e);
        }
    }
}