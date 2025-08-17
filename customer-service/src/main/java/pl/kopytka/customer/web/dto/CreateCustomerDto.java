package pl.kopytka.customer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email
) {
}