package pl.kopytka.common.config;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.kopytka.common.domain.event.DomainEventPublisher;

@Configuration
@EnableAsync
public class DomainEventPublisherConfiguration {

    @Bean
    @SuppressWarnings("rawtypes")
    DomainEventPublisher eventsPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }
}
