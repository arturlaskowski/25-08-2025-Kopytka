package pl.kopytka.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.kopytka.common.config.EnableKopytkaCommon;

@SpringBootApplication
@EnableKopytkaCommon
public class OrderApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}
