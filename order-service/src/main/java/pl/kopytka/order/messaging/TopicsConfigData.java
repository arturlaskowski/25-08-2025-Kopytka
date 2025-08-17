package pl.kopytka.order.messaging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "order-service.kafka.topics")
class TopicsConfigData {
    private String customerEvent;
    private String paymentCommand;
}
