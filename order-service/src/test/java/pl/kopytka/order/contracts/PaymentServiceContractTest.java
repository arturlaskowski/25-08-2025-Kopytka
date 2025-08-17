package pl.kopytka.order.contracts;

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
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.order.application.integration.payment.MakePaymentRequest;
import pl.kopytka.order.application.integration.payment.PaymentResult;
import pl.kopytka.order.application.integration.payment.PaymentServiceFeignClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    void should_process_payment_successfully() {
        // given
        OrderId orderId = new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        CustomerId customerId = CustomerId.newOne();
        Money amount = new Money(new BigDecimal("99.99"));
        var makePaymentRequest = new MakePaymentRequest(orderId.id(), customerId.id(), amount.amount());

        // when
        PaymentResult result = paymentServiceFeignClient.processPayment(makePaymentRequest);

        // then
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void should_fail_payment_when_insufficient_funds() {
        // given
        OrderId orderId = new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        CustomerId customerId = CustomerId.newOne();
        Money amount = new Money(new BigDecimal("1000.00"));
        var makePaymentRequest = new MakePaymentRequest(orderId.id(), customerId.id(), amount.amount());

        // when
        PaymentResult result = paymentServiceFeignClient.processPayment(makePaymentRequest);

        // then
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isNotNull();
    }
}
