package pl.kopytka.restaurant.domain.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record OrderApprovalId(UUID orderApprovalId) {

    public static OrderApprovalId newOne() {
        return new OrderApprovalId(UUID.randomUUID());
    }

    public UUID id() {
        return orderApprovalId;
    }
}
