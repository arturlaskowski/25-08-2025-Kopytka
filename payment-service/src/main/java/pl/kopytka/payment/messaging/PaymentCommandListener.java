package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.CancelPaymentAvroCommand;
import pl.kopytka.avro.payment.CreatePaymentAvroCommand;
import pl.kopytka.avro.payment.PaymentCommandAvroModel;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;
import pl.kopytka.payment.application.PaymentApplicationService;
import pl.kopytka.payment.application.dto.CancelPaymentCommand;
import pl.kopytka.payment.application.dto.MakePaymentCommand;

import java.util.List;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Component
@RequiredArgsConstructor
class PaymentCommandListener extends AbstractKafkaConsumer<PaymentCommandAvroModel> {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    @KafkaListener(id = "PaymentCommandListener",
            groupId = "${payment-service.kafka.group-id}",
            topics = "${payment-service.kafka.topics.payment-command}")
    public void receive(@Payload List<PaymentCommandAvroModel> messages,
                        @Header(RECEIVED_KEY) List<String> keys,
                        @Header(RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(PaymentCommandAvroModel command) {
        switch (command.getType()) {
            case CREATE_PAYMENT -> handleCreatePayment(command);
            case CANCEL_PAYMENT -> handleCancelPayment(command);
            default -> log.warn("Unknown payment command type: {}", command.getType());
        }
    }

    private void handleCreatePayment(PaymentCommandAvroModel command) {
        var avroCommand = (CreatePaymentAvroCommand) command.getPayload();
        var makePaymentCommand = new MakePaymentCommand(
                avroCommand.getOrderId(),
                avroCommand.getCustomerId(),
                avroCommand.getPrice()
        );
        paymentApplicationService.makePayment(makePaymentCommand);
        log.info("Payment creation command processed for order: {}", avroCommand.getOrderId());
    }

    private void handleCancelPayment(PaymentCommandAvroModel command) {
        var avroCommand = (CancelPaymentAvroCommand) command.getPayload();
        var cancelPaymentCommand = new CancelPaymentCommand(
                avroCommand.getOrderId(),
                avroCommand.getCustomerId()
        );
        paymentApplicationService.cancelPayment(cancelPaymentCommand);
        log.info("Payment cancellation command processed for order: {}", avroCommand.getOrderId());
    }

    @Override
    protected String getMessageTypeName() {
        return "paymentCommand";
    }
}
