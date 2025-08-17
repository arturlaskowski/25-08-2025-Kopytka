package pl.kopytka.order.application.integration.customer;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
