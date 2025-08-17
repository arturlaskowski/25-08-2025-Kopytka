package pl.kopytka.order.application.integration.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.web.ServiceUnavailableException;
import pl.kopytka.common.web.dto.MakePaymentRequest;
import pl.kopytka.common.web.dto.PaymentResult;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    public void processPayment(OrderId orderId, CustomerId customerId, Money amount) {
        try {
            String url = paymentServiceUrl + "/api/payments/process";
            log.info("Processing payment for order: {}, customer: {}, amount: {}",
                    orderId.id(), customerId.id(), amount.amount());

            MakePaymentRequest request = new MakePaymentRequest(
                    orderId.id(),
                    customerId.id(),
                    amount.amount()
            );

            ResponseEntity<PaymentResult> response = restTemplate.postForEntity(url, request, PaymentResult.class);

            // Check for null response body first
            if (response.getBody() == null) {
                log.error("Payment service returned empty response body");
                throw new PaymentProcessingFailedException(orderId, customerId, amount,
                        "Payment service returned empty response");
            }

            // Handle payment failure
            if (!response.getBody().success()) {
                log.warn("Payment processing failed: {}", response.getBody().errorMessage());
                throw new PaymentProcessingFailedException(orderId, customerId, amount,
                        response.getBody().errorMessage());
            }

            log.info("Payment processed successfully for order: {}", orderId.id());

        } catch (HttpClientErrorException e) {
            log.error("Payment processing failed with status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceUnavailableException("Payment", "processing payment", e);

        } catch (Exception e) {
            if (e instanceof PaymentProcessingFailedException ||
                    e instanceof ServiceUnavailableException) {
                throw e;
            }

            log.error("Error while processing payment: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Payment", "processing payment", e);
        }
    }
}