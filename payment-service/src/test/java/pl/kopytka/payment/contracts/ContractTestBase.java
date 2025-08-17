package pl.kopytka.payment.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import pl.kopytka.payment.PaymentApp;
import pl.kopytka.payment.application.PaymentApplicationService;
import pl.kopytka.payment.application.WalletService;
import pl.kopytka.payment.application.dto.MakePaymentCommand;
import pl.kopytka.payment.application.dto.PaymentResult;
import pl.kopytka.payment.domain.PaymentId;
import pl.kopytka.payment.domain.WalletId;
import pl.kopytka.payment.web.dto.CreateWalletRequest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PaymentApp.class)
@TestPropertySource(properties = {"eureka.client.enabled=false", "kopytka.scheduling.enabled=false"})
public abstract class ContractTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private PaymentApplicationService paymentApplicationService;

    @MockitoBean
    private WalletService walletService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);

        // Mock wallet creation
        when(walletService.createWallet(any(CreateWalletRequest.class)))
                .thenReturn(WalletId.newOne());


        PaymentResult successResult = new PaymentResult(
                PaymentId.newOne().paymentId(),
                true,
                null
        );

        PaymentResult failureResult = new PaymentResult(
                PaymentId.newOne().paymentId(),
                false,
                "Error Message"
        );

        // Use conditional mock based on the orderId in the command
        when(paymentApplicationService.makePayment(any(MakePaymentCommand.class)))
                .thenAnswer(invocation -> {
                    MakePaymentCommand command = invocation.getArgument(0);
                    if (command.orderId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                        return successResult;
                    } else if (command.orderId().equals(UUID.fromString("00000000-0000-0000-0000-000000000001"))) {
                        return failureResult;
                    } else {
                        // Default case for other order IDs
                        return successResult;
                    }
                });
    }
}

