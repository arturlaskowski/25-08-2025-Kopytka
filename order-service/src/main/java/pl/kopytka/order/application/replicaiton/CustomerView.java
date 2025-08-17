package pl.kopytka.order.application.replicaiton;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerView {

    @Id
    private UUID customerId;

    public CustomerView(UUID customerId) {
        this.customerId = customerId;
    }
}
