package pl.kopytka.order.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderAddressDto(
    @NotNull String street,
    @NotNull String postCode,
    @NotNull String city,
    @NotNull String houseNo
) {}
