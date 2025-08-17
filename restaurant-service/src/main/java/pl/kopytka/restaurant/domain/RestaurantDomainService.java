package pl.kopytka.restaurant.domain;

import org.springframework.stereotype.Service;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.restaurant.domain.entity.Restaurant;
import pl.kopytka.restaurant.domain.exception.RestaurantDomainException;

@Service
public class RestaurantDomainService {

    public Restaurant createRestaurant(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RestaurantDomainException("Restaurant name cannot be null or empty");
        }
        return new Restaurant(name);
    }

    public void addProductToRestaurant(Restaurant restaurant, String productName, Money price) {
        if (restaurant == null) {
            throw new RestaurantDomainException("Restaurant cannot be null");
        }
        if (!restaurant.isAvailable()) {
            throw new RestaurantDomainException("Cannot add products to inactive restaurant");
        }

        restaurant.addProduct(productName, price);
    }

    public void activateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new RestaurantDomainException("Restaurant cannot be null");
        }
        restaurant.activate();
    }

    public void deactivateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new RestaurantDomainException("Restaurant cannot be null");
        }
        restaurant.deactivate();
    }
}
