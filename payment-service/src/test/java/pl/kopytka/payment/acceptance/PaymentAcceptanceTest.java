package pl.kopytka.payment.acceptance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kopytka.common.AcceptanceTest;
import pl.kopytka.common.BaseIntegrationTest;
import pl.kopytka.payment.web.dto.CreateWalletRequest;
import pl.kopytka.payment.web.dto.MakePaymentRequest;
import pl.kopytka.payment.web.dto.PaymentResultResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AcceptanceTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentAcceptanceTest extends BaseIntegrationTest {

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

        var response = testRestTemplate.postForEntity(getBaseWalletsUrl(), createWalletRequest, Void.class);
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
        var response = testRestTemplate.postForEntity(getPaymentsProcessUrl(), request, PaymentResultResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .hasFieldOrProperty("paymentId")
                .hasFieldOrPropertyWithValue("success", true)
                .hasFieldOrPropertyWithValue("errorMessage", null);
    }

    @Test
    @DisplayName("""
            given payment request with to large amount,
            when request is sent,
            then payment is processed and HTTP 200 is returned with errorMessage""")
    void givenToLargeAmountInPaymentRequest_whenRequestIsSent_thenPaymentProcessedAndHttp200Returned() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(10000);

        MakePaymentRequest request = new MakePaymentRequest(
                orderId,
                customerId,
                price
        );

        // when
        var response = testRestTemplate.postForEntity(getPaymentsProcessUrl(), request, PaymentResultResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .hasFieldOrProperty("paymentId")
                .hasFieldOrPropertyWithValue("success", false)
                .extracting("errorMessage")
                .asString()
                .contains("must be less than or equal to wallet amount");
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
        makePaymentExpectingError(
                orderId,
                amount,
                HttpStatus.BAD_REQUEST,
                "Payment already exists"
        );
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
        cancelPaymentExpectingError(
                orderId,
                HttpStatus.BAD_REQUEST,
                "Payment is already cancelled"
        );
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
        cancelPaymentExpectingError(
                orderId,
                differentCustomerId,
                HttpStatus.BAD_REQUEST,
                "Customer ID does not match the payment"
        );
    }

    private void makePaymentSuccessfully(UUID orderId, BigDecimal amount) {
        MakePaymentRequest request = new MakePaymentRequest(orderId, customerId, amount);
        ResponseEntity<Void> response = testRestTemplate.postForEntity(getPaymentsProcessUrl(), request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void makePaymentExpectingError(UUID orderId, BigDecimal amount,
                                           HttpStatus expectedStatus, String expectedErrorMessage) {
        MakePaymentRequest request = new MakePaymentRequest(orderId, customerId, amount);
        ResponseEntity<String> response = testRestTemplate.postForEntity(getPaymentsProcessUrl(), request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).contains(expectedErrorMessage);
    }

    private void cancelPaymentSuccessfully(UUID orderId) {
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                getPaymentsCancelUrl(orderId), null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void cancelPaymentExpectingError(UUID orderId,
                                             HttpStatus expectedStatus, String expectedErrorMessage) {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                getPaymentsCancelUrl(orderId), null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).contains(expectedErrorMessage);
    }

    private void cancelPaymentExpectingError(UUID orderId, UUID specificCustomerId,
                                             HttpStatus expectedStatus, String expectedErrorMessage) {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                getPaymentsCancelUrl(orderId, specificCustomerId), null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).contains(expectedErrorMessage);
    }

    private String getPaymentsProcessUrl() {
        return "http://localhost:" + port + "/api/payments/process";
    }

    private String getPaymentsCancelUrl(UUID orderId) {
        return "http://localhost:" + port + "/api/payments/cancel?paymentId=" + orderId + "&customerId=" + customerId;
    }

    private String getPaymentsCancelUrl(UUID orderId, UUID specificCustomerId) {
        return "http://localhost:" + port + "/api/payments/cancel?paymentId=" + orderId + "&customerId=" + specificCustomerId;
    }

    private String getBaseWalletsUrl() {
        return "http://localhost:" + port + "/api/wallets";
    }
}
