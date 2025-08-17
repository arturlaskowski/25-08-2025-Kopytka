package pl.kopytka.common.inbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test message processor for inbox pattern integration tests.
 * This processor now contains the actual business logic that should be executed
 * once per message, while the IdempotentKafkaConsumer handles the idempotency.
 */
@TestComponent
@Slf4j
class TestMessageProcessor {

    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final List<String> processedMessages = Collections.synchronizedList(new ArrayList<>());

    public void processBusinessLogic(String message) {
        processedMessages.add(message);
        processedCount.incrementAndGet();
        log.debug("Test message processed: {}", message);
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public List<String> getProcessedMessages() {
        return new ArrayList<>(processedMessages);
    }

    public void reset() {
        processedCount.set(0);
        processedMessages.clear();
        log.debug("Test message processor reset");
    }
}
