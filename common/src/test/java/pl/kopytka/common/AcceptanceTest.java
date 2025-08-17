package pl.kopytka.common;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to configure an embedded PostgreSQL database for testing.
 * This annotation combines several Spring Boot test annotations to simplify test configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.driver-class-name=org.postgresql.Driver"
})
@AutoConfigureEmbeddedDatabase(provider = DatabaseProvider.ZONKY)
@KafkaTestConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public @interface AcceptanceTest {
    /**
     * Define topics for the embedded Kafka broker.
     * @return array of topic names
     */
    @AliasFor(annotation = KafkaTestConfig.class, attribute = "topics")
    String[] topics() default { "test-topic" };

    /**
     * Number of partitions for the topics.
     * @return number of partitions
     */
    @AliasFor(annotation = KafkaTestConfig.class, attribute = "partitions")
    int partitions() default 1;
}