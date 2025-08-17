package pl.kopytka.common.inbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import pl.kopytka.common.kafka.consumer.IdempotentKafkaConsumer;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Test Kafka message consumer for inbox pattern integration tests.
 * This consumer extends IdempotentKafkaConsumer to demonstrate the real implementation
 * using the Inbox pattern for idempotent message processing.
 */
@TestComponent
@Slf4j
@RequiredArgsConstructor
class TestKafkaMessageConsumer extends IdempotentKafkaConsumer<TestEventAvroModel> {

    private final TestMessageProcessor messageProcessor;

    @KafkaListener(topics = "test-events", groupId = "test-inbox-group")
    public void listenToStringMessages(@Payload List<String> stringMessages,
                                       @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                                       @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        // Convert string messages to TestEventAvroModel
        List<TestEventAvroModel> messages = IntStream.range(0, stringMessages.size())
                .mapToObj(i -> {
                    String messageId = keys.get(i);
                    String content = stringMessages.get(i);
                    return new TestEventAvroModel(messageId, content);
                })
                .toList();

        // Call the parent method with converted messages
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(TestEventAvroModel event) {
        messageProcessor.processBusinessLogic(event.getContent());
        log.debug("Processed test event with content: {}", event.getContent());
    }

    @Override
    protected String getMessageTypeName() {
        return "testEvent";
    }
}
