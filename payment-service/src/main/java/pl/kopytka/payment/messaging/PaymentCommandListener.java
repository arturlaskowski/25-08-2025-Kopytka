package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.payment.ProcessPaymentCommandAvroModel;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;
import pl.kopytka.payment.application.PaymentApplicationService;
import pl.kopytka.payment.application.dto.MakePaymentCommand;

import java.util.List;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Component
@RequiredArgsConstructor
class PaymentCommandListener extends AbstractKafkaConsumer<ProcessPaymentCommandAvroModel> {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    @KafkaListener(id = "PaymentCommandListener",
            groupId = "${payment-service.kafka.group-id}",
            topics = "${payment-service.kafka.topics.payment-command}")
    public void receive(@Payload List<ProcessPaymentCommandAvroModel> messages,
                        @Header(RECEIVED_KEY) List<String> keys,
                        @Header(RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(ProcessPaymentCommandAvroModel message) {
        var makePaymentCommand = new MakePaymentCommand(message.getOrderId(),
                message.getCustomerId(),
                message.getPrice());
        paymentApplicationService.makePayment(makePaymentCommand);
    }

    @Override
    protected String getMessageTypeName() {
        return "paymentCommand";
    }
}
