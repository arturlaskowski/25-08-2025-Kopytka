package pl.kopytka.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kopytka.common.domain.valueobject.CustomerId;

@Entity(name = "customers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Customer {

    @Id
    private CustomerId customerId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @Version
    private int version;

    public Customer(String firstName, String lastName, String email) {
        this.customerId = CustomerId.newOne();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
