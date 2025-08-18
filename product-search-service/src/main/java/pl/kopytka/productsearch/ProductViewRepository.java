package pl.kopytka.productsearch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ProductViewRepository extends JpaRepository<ProductView, UUID> {

    List<ProductView> findByRestaurantId(UUID restaurantId);

}
