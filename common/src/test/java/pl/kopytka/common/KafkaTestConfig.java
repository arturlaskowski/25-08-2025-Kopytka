package pl.kopytka.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EmbeddedKafka
public @interface KafkaTestConfig {
    @AliasFor(annotation = EmbeddedKafka.class, attribute = "topics")
    String[] topics() default {};

    @AliasFor(annotation = EmbeddedKafka.class, attribute = "partitions")
    int partitions() default 1;
}