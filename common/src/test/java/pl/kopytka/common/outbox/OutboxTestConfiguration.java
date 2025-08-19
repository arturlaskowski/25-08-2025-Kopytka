package pl.kopytka.common.outbox;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Outbox Pattern integration tests.
 * Provides test-specific implementations of outbox components.
 */
@TestConfiguration
class OutboxTestConfiguration {

    @Bean
    @Primary
    public TestOutboxPublisher testOutboxPublisher() {
        return new TestOutboxPublisher();
    }
}
