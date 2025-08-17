package pl.kopytka.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Base configuration for Feign clients.
 * Services that use Feign clients should import this configuration.
 */
@Configuration
@EnableFeignClients(basePackages = {"pl.kopytka"})
public class FeignConfig {
}
