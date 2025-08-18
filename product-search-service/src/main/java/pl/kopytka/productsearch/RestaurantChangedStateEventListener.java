package pl.kopytka.productsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.restaurant.RestaurantChangedEventAvroModel;
import pl.kopytka.common.kafka.consumer.AbstractKafkaConsumer;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class RestaurantChangedStateEventListener extends AbstractKafkaConsumer<RestaurantChangedEventAvroModel> {

    private final ProductViewRepository productViewRepository;

    @Override
    @KafkaListener(id = "RestaurantChangedStateEventListener",
            groupId = "${product-search-service.kafka.group-id}",
            topics = "${product-search-service.kafka.topics.restaurant-state-event}")
    public void receive(@Payload List<RestaurantChangedEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        super.receive(messages, keys, partitions, offsets);
    }

    @Override
    protected void processMessage(RestaurantChangedEventAvroModel event) {
        List<ProductView> existingProducts = productViewRepository.findByRestaurantId(event.getRestaurantId());

        // Sprawdź czy istnieją produkty dla tej restauracji
        if (!existingProducts.isEmpty()) {
            // Sprawdź wersję - jeśli aktualna wersja jest nowsza lub równa, nie rób nic
            ProductView firstProduct = existingProducts.getFirst();
            if (firstProduct.getVersion() >= event.getVersion()) {
                log.info("Skipping restaurant {} update - current version {} is not older than event version {}",
                        event.getRestaurantId(), firstProduct.getVersion(), event.getVersion());
                return;
            }

            // Usuń wszystkie istniejące produkty dla tej restauracji
            productViewRepository.deleteAll(existingProducts);
            log.debug("Deleted {} existing products for restaurant {}", existingProducts.size(), event.getRestaurantId());
        }

        // Utwórz nowe ProductView na podstawie danych z eventu
        List<ProductView> newProducts = event.getProducts().stream()
                .map(product -> ProductView.builder()
                        .productId(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .available(product.getAvailable())
                        .restaurantId(event.getRestaurantId())
                        .restaurantName(event.getName())
                        .restaurantAvailable(event.getAvailable())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .version(event.getVersion())
                        .build())
                .toList();


        productViewRepository.saveAll(newProducts);

        log.info("Successfully replicated {} products for restaurant {} (version {})",
                newProducts.size(), event.getRestaurantId(), event.getVersion());
    }

    @Override
    protected String getMessageTypeName() {
        return "restaurantChangedStateEvent";
    }
}