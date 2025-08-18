package pl.kopytka.productsearch;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_views")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
class ProductView {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private String name;

    private BigDecimal price;

    private boolean available;

    private UUID restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private boolean restaurantAvailable;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private int version;
}