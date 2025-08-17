package pl.kopytka.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.kopytka.common.config.EnableKopytkaCommon;

@EnableKopytkaCommon
@SpringBootApplication
public class RestaurantApp {
    
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApp.class, args);
    }
}
