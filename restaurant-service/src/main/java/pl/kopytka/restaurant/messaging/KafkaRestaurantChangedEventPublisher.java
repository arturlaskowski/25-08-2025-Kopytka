package pl.kopytka.restaurant.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.kopytka.avro.restaurant.RestaurantChangedEventAvroModel;
import pl.kopytka.avro.restaurant.RestaurantProduct;
import pl.kopytka.restaurant.domain.RestaurantChangedEventPublisher;
import pl.kopytka.restaurant.domain.entity.Restaurant;

@Component
@RequiredArgsConstructor
class KafkaRestaurantChangedEventPublisher implements RestaurantChangedEventPublisher {

    private final KafkaTemplate<String, RestaurantChangedEventAvroModel> kafkaTemplate;
    private final TopicsConfigData topicsConfigData;

    @Override
    public void sendEvent(Restaurant restaurant) {
        var restaurantId = restaurant.getId();

        var products = restaurant.getProducts().stream()
                .map(product -> new RestaurantProduct(
                        product.id().id(),
                        product.name(),
                        product.price().amount(),
                        product.available()
                ))
                .toList();

        var eventMessage = new RestaurantChangedEventAvroModel(
                restaurantId.id(),
                restaurant.getName(),
                restaurant.isAvailable(),
                restaurant.getVersion(),
                products
        );

        kafkaTemplate.send(topicsConfigData.getRestaurantStateEvent(), restaurantId.id().toString(), eventMessage);
    }
}
