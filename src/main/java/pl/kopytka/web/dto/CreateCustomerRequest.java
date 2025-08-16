package pl.kopytka.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email
) {
}