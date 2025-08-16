package pl.kopytka.order.application.dto;

public record OrderAddressDto(
        String street,
        String postCode,
        String city,
        String houseNo) {
}