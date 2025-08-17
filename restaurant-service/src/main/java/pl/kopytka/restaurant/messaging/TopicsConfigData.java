package pl.kopytka.restaurant.messaging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "restaurant-service.kafka.topics")
class TopicsConfigData {
    private String restaurantOrderCommand;
    private String restaurantOrderEvent;
}
