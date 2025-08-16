package pl.kopytka.application.dto;

public record OrderAddressDto(
        String street,
        String postCode,
        String city,
        String houseNo) {
}