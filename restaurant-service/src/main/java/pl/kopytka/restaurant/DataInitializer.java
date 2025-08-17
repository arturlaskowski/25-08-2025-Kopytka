package pl.kopytka.restaurant;


import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Configuration
@Profile("!prod")
class DataInitializer {

    // Dodaje domyślną restaurację, żeby lokalnie szybciej można było testować, w kolekcji Postmana są używane te zasoby
    @Bean
    public ApplicationRunner dataSqlRunner(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        return args -> {
            Resource resource = resourceLoader.getResource("classpath:data.sql");
            String sql = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
        };
    }
}