package pl.kopytka.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.kopytka.common.config.EnableKopytkaCommon;

@SpringBootApplication
@EnableKopytkaCommon
public class CustomerApp {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApp.class, args);
    }
}
