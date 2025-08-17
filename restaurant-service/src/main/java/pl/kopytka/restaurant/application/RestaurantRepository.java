package pl.kopytka.restaurant.application;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.restaurant.domain.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, RestaurantId> {
}
