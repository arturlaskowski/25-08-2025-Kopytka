package pl.kopytka.restaurant.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kopytka.restaurant.application.dto.ProductProjection;
import pl.kopytka.restaurant.application.dto.RestaurantQuery;
import pl.kopytka.restaurant.domain.entity.Product;
import pl.kopytka.restaurant.domain.entity.Restaurant;

@Mapper(componentModel = "spring")
interface RestaurantMapper {

    @Mapping(target = "id", source = "id.restaurantId")
    RestaurantQuery toProjection(Restaurant restaurant);

    @Mapping(target = "id", source = "id.productId")
    @Mapping(target = "price", source = "price.amount")
    ProductProjection toProjection(Product product);
}
