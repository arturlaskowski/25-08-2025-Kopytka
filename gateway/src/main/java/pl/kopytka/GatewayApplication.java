package pl.kopytka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator kopytkaRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                // Customer Service Routes
                .route(p -> p
                        .path("/api/customers/**")
                        .filters(f -> f.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://CUSTOMER-SERVICE"))

                // Order Service Routes
                .route(p -> p
                        .path("/api/orders/**")
                        .filters(f -> f.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://ORDER-SERVICE"))

                // Payment Service Routes - Payments
                .route(p -> p
                        .path("/api/payments/**")
                        .filters(f -> f.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://PAYMENT-SERVICE"))

                // Payment Service Routes - Wallets
                .route(p -> p
                        .path("/api/wallets/**")
                        .filters(f -> f.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://PAYMENT-SERVICE"))
                .build();
    }
}