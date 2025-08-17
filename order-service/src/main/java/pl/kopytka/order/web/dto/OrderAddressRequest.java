package pl.kopytka.order.web.dto;

import jakarta.validation.constraints.NotNull;

public record OrderAddressRequest(
    @NotNull String street,
    @NotNull String postCode,
    @NotNull String city,
    @NotNull String houseNo
) {}
