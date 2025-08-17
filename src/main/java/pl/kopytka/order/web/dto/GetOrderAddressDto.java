package pl.kopytka.order.web.dto;

public record GetOrderAddressDto(
        String street,
        String postCode,
        String city,
        String houseNo) {
}
