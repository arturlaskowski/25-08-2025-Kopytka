package pl.kopytka.order.application.integration.payment;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.web.ServiceUnavailableException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final PaymentServiceFeignClient paymentServiceFeignClient;

    public void processPayment(OrderId orderId, CustomerId customerId, Money amount) {
        try {
            log.info("Processing payment for order: {}, customer: {}, amount: {}",
                    orderId.id(), customerId.id(), amount.amount());

            MakePaymentRequest request = new MakePaymentRequest(
                    orderId.id(),
                    customerId.id(),
                    amount.amount()
            );

            PaymentResult response = paymentServiceFeignClient.processPayment(request);

            if (response == null) {
                log.error("Payment service returned empty response");
                throw new PaymentProcessingFailedException(orderId, customerId, amount,
                        "Payment service returned empty response");
            }

            if (!response.success()) {
                log.warn("Payment processing failed: {}", response.errorMessage());
                throw new PaymentProcessingFailedException(orderId, customerId, amount, response.errorMessage());
            }

            log.info("Payment processed successfully for order: {}", orderId.id());

        } catch (FeignException e) {
            log.error("Payment processing failed with status: {}, message: {}", e.status(), e.getMessage());

            // If status is 5xx payment service issue
            if (HttpStatus.Series.valueOf(e.status()) == HttpStatus.Series.SERVER_ERROR) {
                throw new ServiceUnavailableException("Payment", "processing payment", e);
            }

            // For all other non-200 status codes - payment processing issue
            throw new PaymentProcessingFailedException(orderId, customerId, amount, extractErrorMessage(e));

        } catch (Exception e) {
            if (e instanceof PaymentProcessingFailedException || e instanceof ServiceUnavailableException) {
                throw e;
            }

            log.error("Unexpected error while processing payment: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Payment", "processing payment", e);
        }
    }

    private String extractErrorMessage(FeignException e) {
        try {
            return e.contentUTF8() != null ? e.contentUTF8() : e.getMessage();
        } catch (Exception ex) {
            return e.getMessage();
        }
    }
}