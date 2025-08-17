package pl.kopytka.restaurant.web.dto;

import java.math.BigDecimal;

public record AddProductRequest(String productName, BigDecimal price) {
}
