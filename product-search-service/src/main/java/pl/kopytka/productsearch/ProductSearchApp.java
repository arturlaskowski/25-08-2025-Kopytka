package pl.kopytka.productsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.kopytka.common.config.EnableKopytkaCommon;

@SpringBootApplication
@EnableKopytkaCommon
public class ProductSearchApp {

    public static void main(String[] args) {
        SpringApplication.run(ProductSearchApp.class, args);
    }
}
