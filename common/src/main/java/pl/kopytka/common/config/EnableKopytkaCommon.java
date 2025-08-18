package pl.kopytka.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        SchedulingConfig.class,
        DomainEventPublisherConfiguration.class
})
@ComponentScan("pl.kopytka")
@EnableJpaRepositories(basePackages = "pl.kopytka")
@EntityScan(basePackages = "pl.kopytka")
public @interface EnableKopytkaCommon {
}
