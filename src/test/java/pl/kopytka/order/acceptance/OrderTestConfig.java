package pl.kopytka.order.acceptance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class OrderTestConfig {

    @Bean
    @Primary
    public StubCustomerFacade customerFacade() {
        return new StubCustomerFacade();
    }
}
