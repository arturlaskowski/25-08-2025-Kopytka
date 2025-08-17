package pl.kopytka.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable Kopytka common configurations.
 * This will automatically configure Feign clients, distributed locking, and scheduling
 * based on application properties.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        SchedulingConfig.class
})
@ComponentScan("pl.kopytka")
public @interface EnableKopytkaCommon {
}
