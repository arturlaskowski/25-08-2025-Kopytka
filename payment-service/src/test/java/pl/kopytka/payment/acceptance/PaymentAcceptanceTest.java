package pl.kopytka.payment.acceptance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kopytka.payment.web.dto.CancelPaymentRequest;
import pl.kopytka.payment.web.dto.CreateWalletRequest;
import pl.kopytka.payment.web.dto.MakePaymentRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID customerId;

    @BeforeAll
    void setupWallet() {
        // Create a customer ID to be used across all tests
        customerId = UUID.randomUUID();

        // Create a wallet with sufficient funds for all tests
        CreateWalletRequest createWalletRequest = new CreateWalletRequest(
                customerId,
                new BigDecimal("1000.00")  // Enough balance for all test payments
        );

        var response = restTemplate.postForEntity(getBaseWalletsUrl(), createWalletRequest, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("""
            given valid payment request,
            when request is sent,
            then payment is processed and HTTP 200 is returned""")
    void givenValidPaymentRequest_whenRequestIsSent_thenPaymentProcessedAndHttp200Returned() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(49.99);

        MakePaymentRequest request = new MakePaymentRequest(
                orderId,
                customerId,
                price
        );

        // when
        var response = restTemplate.postForEntity(getPaymentsProcessUrl(), request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("""
            given existing payment,
            when payment is processed again,
            then error is returned""")
    void givenExistingPayment_whenPaymentIsProcessedAgain_thenErrorIsReturned() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(25.00);

        // first payment successful
        makePaymentSuccessfully(orderId, amount);

        // when & then - second payment attempt fails
        MakePaymentRequest request = new MakePaymentRequest(orderId, customerId, amount);
        ResponseEntity<String> response = restTemplate.postForEntity(getPaymentsProcessUrl(), request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Payment already exists");
    }

    @Test
    @DisplayName("""
            given cancelled payment,
            when payment is cancelled again,
            then error is returned""")
    void givenCancelledPayment_whenPaymentIsCancelledAgain_thenErrorIsReturned() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(30.00);

        // process and cancel payment
        makePaymentSuccessfully(orderId, amount);
        cancelPaymentSuccessfully(orderId);

        // when & then - second cancellation attempt fails
        CancelPaymentRequest request = new CancelPaymentRequest(orderId, customerId);
        ResponseEntity<String> response = restTemplate.postForEntity(
                getPaymentsCancelUrl(orderId), request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Payment is already cancelled");
    }

    @Test
    @DisplayName("""
            given payment with incorrect customer ID,
            when cancellation is attempted,
            then error is returned""")
    void givenPaymentWithIncorrectCustomerId_whenCancellationIsAttempted_thenErrorIsReturned() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID differentCustomerId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(45.00);

        // process payment for actual customer
        makePaymentSuccessfully(orderId, amount);

        // when & then - cancellation with different customer ID fails
        CancelPaymentRequest request = new CancelPaymentRequest(orderId, differentCustomerId);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                getPaymentsCancelUrl(orderId, differentCustomerId), request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void makePaymentSuccessfully(UUID orderId, BigDecimal amount) {
        MakePaymentRequest request = new MakePaymentRequest(orderId, customerId, amount);
        ResponseEntity<Void> response = restTemplate.postForEntity(getPaymentsProcessUrl(), request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void cancelPaymentSuccessfully(UUID orderId) {
        CancelPaymentRequest request = new CancelPaymentRequest(orderId, customerId);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                getPaymentsCancelUrl(orderId), request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String getPaymentsProcessUrl() {
        return "http://localhost:" + port + "/api/payments/process";
    }

    private String getPaymentsCancelUrl(UUID orderId) {
        return "http://localhost:" + port + "/api/payments/cancel?orderId=" + orderId + "&customerId=" + customerId;
    }

    private String getPaymentsCancelUrl(UUID orderId, UUID specificCustomerId) {
        return "http://localhost:" + port + "/api/payments/cancel?orderId=" + orderId + "&customerId=" + specificCustomerId;
    }

    private String getBaseWalletsUrl() {
        return "http://localhost:" + port + "/api/wallets";
    }
}
