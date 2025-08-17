package pl.kopytka.customer.messaging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "customer-service.kafka.topics")
class TopicsConfigData {
    private String customerEvent;
}
