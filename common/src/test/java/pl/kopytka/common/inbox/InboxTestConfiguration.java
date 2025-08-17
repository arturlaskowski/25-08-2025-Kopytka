package pl.kopytka.common.inbox;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Inbox Pattern integration tests.
 * Provides test-specific implementations of inbox components.
 */
@TestConfiguration
class InboxTestConfiguration {

    @Bean
    @Primary
    public TestMessageProcessor testMessageProcessor() {
        return new TestMessageProcessor();
    }

    @Bean
    @Primary
    public TestKafkaMessageConsumer testKafkaMessageConsumer(TestMessageProcessor testMessageProcessor) {
        return new TestKafkaMessageConsumer(testMessageProcessor);
    }
}
