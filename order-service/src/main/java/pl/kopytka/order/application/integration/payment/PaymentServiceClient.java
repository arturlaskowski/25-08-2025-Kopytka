package pl.kopytka.order.application.integration.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.kopytka.order.domain.CustomerId;
import pl.kopytka.order.domain.Money;
import pl.kopytka.order.domain.OrderId;

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

            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(url, request, PaymentResponse.class);

            // Check for null response body first
            if (response.getBody() == null) {
                log.error("Payment service returned empty response body");
                throw new PaymentServiceUnavailableException(orderId, customerId, amount,
                        new RuntimeException("Payment service returned empty response"));
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
            throw new PaymentServiceUnavailableException(orderId, customerId, amount, e);

        } catch (Exception e) {
            if (e instanceof PaymentProcessingFailedException ||
                    e instanceof PaymentServiceUnavailableException) {
                throw e;
            }

            log.error("Error while processing payment: {}", e.getMessage(), e);
            throw new PaymentServiceUnavailableException(orderId, customerId, amount, e);
        }
    }
}