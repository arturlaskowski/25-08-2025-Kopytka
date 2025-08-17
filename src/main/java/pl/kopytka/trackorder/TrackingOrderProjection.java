package pl.kopytka.trackorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Immutable
@Getter
class TrackingOrderProjection {

    @Id
    private UUID orderId;

    private String status;

    @Column(name = "price")
    private BigDecimal amount;
}
