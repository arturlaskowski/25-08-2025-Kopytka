package pl.kopytka.restaurant.application.dto;

import java.util.List;
import java.util.UUID;

public record RestaurantQuery(
    UUID id,
    String name,
    boolean available,
    List<ProductProjection> products
) {
}
