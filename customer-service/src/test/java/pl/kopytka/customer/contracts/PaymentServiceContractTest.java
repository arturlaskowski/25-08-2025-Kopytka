package pl.kopytka.customer.contracts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.customer.application.integration.payment.CreateWalletRequest;
import pl.kopytka.customer.application.integration.payment.PaymentServiceFeignClient;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.LOCAL;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"eureka.client.enabled=false", "kopytka.scheduling.enabled=false"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(
        stubsMode = LOCAL,
        ids = "pl.kopytka.now:payment-service:+:stubs:9582")
class PaymentServiceContractTest {

    @Autowired
    private PaymentServiceFeignClient paymentServiceFeignClient;

    @Test
    void should_create_wallet_successfully() {
        // given
        var customerId = CustomerId.newOne();
        var createWalletRequest = new CreateWalletRequest(customerId.id(), new BigDecimal("0.00"));

        // when & then
        assertThatNoException().isThrownBy(() ->
                paymentServiceFeignClient.createWallet(createWalletRequest)
        );
    }
}
