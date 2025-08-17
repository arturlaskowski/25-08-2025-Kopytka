package pl.kopytka.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.kopytka.common.config.EnableKopytkaCommon;

@SpringBootApplication
@EnableKopytkaCommon
public class PaymentApp {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }
}
