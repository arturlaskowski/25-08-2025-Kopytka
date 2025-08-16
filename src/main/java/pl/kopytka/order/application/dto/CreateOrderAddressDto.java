package pl.kopytka.order.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderAddressDto(
        @NotBlank String street,
        @NotBlank String postCode,
        @NotBlank String city,
        @NotBlank String houseNo) {
}