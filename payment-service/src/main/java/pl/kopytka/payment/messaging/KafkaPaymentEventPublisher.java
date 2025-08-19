package pl.kopytka.payment.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.payment.domain.PaymentEventPublisher;
import pl.kopytka.payment.domain.event.PaymentEvent;

@Component
@RequiredArgsConstructor
class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final PaymentOutboxWriter paymentOutboxWriter;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(PaymentEvent event) {
        paymentOutboxWriter.on(event);
    }
}
