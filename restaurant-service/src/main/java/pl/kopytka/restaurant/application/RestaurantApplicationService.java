package pl.kopytka.restaurant.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.restaurant.application.dto.AddProductCommand;
import pl.kopytka.restaurant.application.dto.CreateRestaurantCommand;
import pl.kopytka.restaurant.application.dto.RestaurantQuery;
import pl.kopytka.restaurant.application.exception.RestaurantNotFoundException;
import pl.kopytka.restaurant.domain.RestaurantDomainService;
import pl.kopytka.restaurant.domain.entity.Restaurant;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantApplicationService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantMapper restaurantMapper;

    public RestaurantId createRestaurant(CreateRestaurantCommand command) {
        Restaurant restaurant = restaurantDomainService.createRestaurant(command.name());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return savedRestaurant.getId();
    }

    @Transactional
    public void addProduct(AddProductCommand command) {
        Restaurant restaurant = findRestaurantById(command.restaurantId());
        Money price = new Money(command.price());

        restaurantDomainService.addProductToRestaurant(restaurant, command.productName(), price);
    }

    @Transactional
    public void activateRestaurant(UUID restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        restaurantDomainService.activateRestaurant(restaurant);
    }

    @Transactional
    public void deactivateRestaurant(UUID restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        restaurantDomainService.deactivateRestaurant(restaurant);
    }


    @Transactional(readOnly = true)
    public RestaurantQuery getRestaurantById(UUID restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        return restaurantMapper.toProjection(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantQuery> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(restaurantMapper::toProjection)
                .toList();
    }

    private Restaurant findRestaurantById(UUID restaurantId) {
        return restaurantRepository.findById(new RestaurantId(restaurantId))
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId));
    }
}
