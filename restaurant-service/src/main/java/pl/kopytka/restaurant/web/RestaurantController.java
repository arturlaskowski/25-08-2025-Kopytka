package pl.kopytka.restaurant.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.restaurant.application.RestaurantApplicationService;
import pl.kopytka.restaurant.application.dto.AddProductCommand;
import pl.kopytka.restaurant.application.dto.CreateRestaurantCommand;
import pl.kopytka.restaurant.application.dto.RestaurantQuery;
import pl.kopytka.restaurant.web.dto.AddProductRequest;
import pl.kopytka.restaurant.web.dto.CreateRestaurantRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
class RestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;

    @PostMapping
    public ResponseEntity<RestaurantQuery> createRestaurant(@RequestBody CreateRestaurantRequest request) {
        CreateRestaurantCommand command = new CreateRestaurantCommand(request.name());
        RestaurantId restaurantId = restaurantApplicationService.createRestaurant(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(restaurantId.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantQuery> getRestaurant(@PathVariable UUID restaurantId) {
        RestaurantQuery restaurant = restaurantApplicationService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantQuery>> getAllRestaurants() {
        List<RestaurantQuery> restaurants = restaurantApplicationService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @PostMapping("/{restaurantId}/products")
    public ResponseEntity<Void> addProduct(@PathVariable UUID restaurantId,
                                           @RequestBody AddProductRequest request) {
        AddProductCommand command = new AddProductCommand(restaurantId, request.productName(), request.price());
        restaurantApplicationService.addProduct(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{restaurantId}/activate")
    public ResponseEntity<Void> activateRestaurant(@PathVariable UUID restaurantId) {
        restaurantApplicationService.activateRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{restaurantId}/deactivate")
    public ResponseEntity<Void> deactivateRestaurant(@PathVariable UUID restaurantId) {
        restaurantApplicationService.deactivateRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
