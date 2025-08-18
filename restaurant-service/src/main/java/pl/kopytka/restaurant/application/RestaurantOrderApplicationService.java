package pl.kopytka.restaurant.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.domain.event.DomainEventPublisher;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.domain.valueobject.ProductId;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.restaurant.application.dto.ProductDto;
import pl.kopytka.restaurant.domain.entity.OrderApproval;
import pl.kopytka.restaurant.domain.entity.Restaurant;
import pl.kopytka.restaurant.domain.event.OrderApprovedEvent;
import pl.kopytka.restaurant.domain.event.OrderRejectedEvent;
import pl.kopytka.restaurant.domain.event.RestaurantOrderEvent;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantOrderApplicationService {

    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final DomainEventPublisher<RestaurantOrderEvent> eventPublisher;

    @Transactional
    public void approveOrder(UUID restaurantId, UUID orderId, List<ProductDto> products) {
        if (restaurantId == null || orderId == null || products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Restaurant ID, Order ID, and products cannot be null or empty");
        }

        try {
            log.info("Processing restaurant approval for order id: {}", orderId);

            var restaurantIdVO = new RestaurantId(restaurantId);
            var orderIdVO = new OrderId(orderId);

            var restaurantOptional = restaurantRepository.findById(restaurantIdVO);
            if (restaurantOptional.isEmpty()) {
                rejectOrderWithReason(restaurantIdVO, orderIdVO, "Restaurant does not exist");
                return;
            }

            var restaurant = restaurantOptional.get();

            // Check if restaurant can process order
            if (!restaurant.isAvailable()) {
                rejectOrderWithReason(restaurantIdVO, orderIdVO, "Restaurant is not available");
                return;
            }

            // Validate all products
            if (areAllProductsValid(restaurant, products)) {
                approveOrderForRestaurant(restaurant, orderIdVO);
            } else {
                rejectOrderWithReason(restaurantIdVO, orderIdVO, "Not all products are available");
            }

        } catch (Exception e) {
            log.error("Error processing restaurant approval for order id: {}", orderId, e);
            handleOrderProcessingFailure(restaurantId, orderId, e.getMessage());
        }
    }

    private boolean areAllProductsValid(Restaurant restaurant, List<ProductDto> products) {
        return products.stream()
                .allMatch(product -> {
                    var productId = new ProductId(product.id());
                    return restaurant.hasProduct(productId);
                });
    }

    private void approveOrderForRestaurant(Restaurant restaurant, OrderId orderId) {
        var orderApproval = OrderApproval.createApprovedOrder(restaurant.getId(), orderId);
        orderApprovalRepository.save(orderApproval);

        var event = new OrderApprovedEvent(
                restaurant.getId().id(),
                orderId.id());

        eventPublisher.publish(event);
        log.info("Order approved for order id: {}", orderId.id());
    }

    private void rejectOrderWithReason(RestaurantId restaurantId, OrderId orderId, String rejectionReason) {
        var orderApproval = OrderApproval.createRejectedOrder(restaurantId, orderId, rejectionReason);
        orderApprovalRepository.save(orderApproval);

        var event = new OrderRejectedEvent(
                restaurantId.id(),
                orderId.id(),
                rejectionReason);

        eventPublisher.publish(event);
        log.info("Order rejected for order id: {} with reason: {}", orderId.id(), rejectionReason);
    }

    private void handleOrderProcessingFailure(UUID restaurantId, UUID orderId, String errorMessage) {
        try {
            var restaurantIdVO = new RestaurantId(restaurantId);
            var orderIdVO = new OrderId(orderId);
            rejectOrderWithReason(restaurantIdVO, orderIdVO, errorMessage);
        } catch (Exception ex) {
            log.error("Could not process failure scenario for order id: {}", orderId, ex);
        }
    }
}
