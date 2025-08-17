package pl.kopytka.order.application.dto;

public record OrderAddressQuery(
    String street,
    String postCode,
    String city,
    String houseNo
) {}
