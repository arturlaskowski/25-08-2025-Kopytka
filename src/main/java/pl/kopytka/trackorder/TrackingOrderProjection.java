package pl.kopytka.trackorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "tracking_order")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackingOrderProjection {

    @Id
    private UUID orderId;

    private String status;

    @Column(name = "price")
    private BigDecimal amount;
}
