package pl.kopytka.restaurant.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.kopytka.common.domain.valueobject.OrderId;
import pl.kopytka.common.domain.valueobject.RestaurantId;

import java.time.LocalDateTime;

@Entity(name = "order_approvals")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderApproval {

    @Id
    private OrderApprovalId id;
    @NotNull
    private RestaurantId restaurantId;
    @NotNull
    private OrderId orderId;
    private OrderApprovalStatus approvalStatus;
    private LocalDateTime decisionTime;
    private String rejectionReason;

    @Version
    private int version;

    public static OrderApproval createApprovedOrder(RestaurantId restaurantId, OrderId orderId) {
        return OrderApproval.builder()
                .id(OrderApprovalId.newOne())
                .restaurantId(restaurantId)
                .orderId(orderId)
                .approvalStatus(OrderApprovalStatus.APPROVED)
                .decisionTime(LocalDateTime.now())
                .build();
    }

    public static OrderApproval createRejectedOrder(RestaurantId restaurantId, OrderId orderId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty");
        }

        return OrderApproval.builder()
                .id(OrderApprovalId.newOne())
                .restaurantId(restaurantId)
                .orderId(orderId)
                .approvalStatus(OrderApprovalStatus.REJECTED)
                .rejectionReason(rejectionReason)
                .decisionTime(LocalDateTime.now())
                .build();
    }
}
