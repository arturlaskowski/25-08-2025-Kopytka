package pl.kopytka.payment.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import pl.kopytka.common.web.dto.CreateWalletRequest;
import pl.kopytka.payment.web.dto.AddFundsRequest;
import pl.kopytka.payment.web.dto.WalletDto;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WalletControllerAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("""
            given valid wallet creation request,
            when request is sent,
            then wallet is created and HTTP 201 status returned with location header""")
    void givenValidWalletCreationRequest_whenRequestIsSent_thenWalletCreatedAndHttp201Returned() {
        // given
        UUID customerId = UUID.randomUUID();
        BigDecimal initialAmount = new BigDecimal("100.00");

        CreateWalletRequest request = new CreateWalletRequest(
                customerId,
                initialAmount
        );

        // when
        var postResponse = restTemplate.postForEntity(getBaseWalletsUrl(), request, Void.class);

        // then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = postResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        var getResponse = restTemplate.getForEntity(location, WalletDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        WalletDto wallet = getResponse.getBody();
        assertThat(wallet).isNotNull();

        // Now we can safely access the wallet object's properties
        assertThat(wallet.customerId()).isEqualTo(customerId.toString());
        assertThat(wallet.balance()).isEqualByComparingTo(initialAmount);
    }

    @Test
    @DisplayName("""
            given wallet exists,
            when funds are added,
            then wallet balance is updated and HTTP 204 No Content is returned""")
    void givenWalletExists_whenFundsAreAdded_thenWalletBalanceIsUpdatedAndHttp204Returned() {
        // given
        UUID customerId = UUID.randomUUID();
        BigDecimal initialAmount = new BigDecimal("100.00");
        BigDecimal additionalAmount = new BigDecimal("50.00");
        BigDecimal expectedFinalAmount = new BigDecimal("150.00");

        // Create wallet first
        CreateWalletRequest createRequest = new CreateWalletRequest(
                customerId,
                initialAmount
        );

        var createResponse = restTemplate.postForEntity(getBaseWalletsUrl(), createRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        String walletPath = location.getPath();
        String walletId = walletPath.substring(walletPath.lastIndexOf('/') + 1);

        // Add funds
        AddFundsRequest addFundsRequest = new AddFundsRequest(additionalAmount);

        // when
        var addFundsResponse = restTemplate.postForEntity(
                getBaseWalletsUrl() + "/" + walletId + "/funds",
                addFundsRequest,
                Void.class
        );

        // then
        assertThat(addFundsResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify the wallet balance was updated
        var getResponse = restTemplate.getForEntity(
                getBaseWalletsUrl() + "/" + walletId,
                WalletDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        WalletDto updatedWallet = getResponse.getBody();
        assertThat(updatedWallet).isNotNull();
        assertThat(updatedWallet.balance()).isEqualByComparingTo(expectedFinalAmount);
    }

    @Test
    @DisplayName("""
            given wallet exists,
            when wallet is retrieved by customer ID,
            then wallet is returned and HTTP 200 OK is returned""")
    void givenWalletExists_whenWalletIsRetrievedByCustomerId_thenWalletIsReturnedAndHttp200Returned() {
        // given
        UUID customerId = UUID.randomUUID();
        BigDecimal initialAmount = new BigDecimal("100.00");

        // Create wallet first
        CreateWalletRequest createRequest = new CreateWalletRequest(
                customerId,
                initialAmount
        );

        var createResponse = restTemplate.postForEntity(getBaseWalletsUrl(), createRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when
        var getResponse = restTemplate.getForEntity(
                getBaseWalletsUrl() + "/customer/" + customerId,
                WalletDto.class
        );

        // then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        WalletDto wallet = getResponse.getBody();
        assertThat(wallet).isNotNull();
        assertThat(wallet.customerId()).isEqualTo(customerId.toString());
        assertThat(wallet.balance()).isEqualByComparingTo(initialAmount);
    }

    @Test
    @DisplayName("""
            given non-existent wallet ID,
            when wallet is requested,
            then HTTP 404 Not Found is returned""")
    void givenNonExistentWalletId_whenWalletIsRequested_thenHttp404NotFoundReturned() {
        // given
        var nonExistentWalletId = UUID.randomUUID();

        // when
        var response = restTemplate.getForEntity(
                getBaseWalletsUrl() + "/" + nonExistentWalletId,
                Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNotNull()
                .extracting("message")
                .asString()
                .contains("Wallet with ID " + nonExistentWalletId + " not found");
    }

    String getBaseWalletsUrl() {
        return "http://localhost:" + port + "/api/wallets";
    }
}
