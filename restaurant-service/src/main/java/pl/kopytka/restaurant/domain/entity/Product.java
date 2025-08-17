package pl.kopytka.restaurant.domain.entity;

import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.ProductId;
import pl.kopytka.restaurant.domain.exception.RestaurantDomainException;

public record Product(
        ProductId id,
        String name,
        Money price,
        boolean available) {

    public Product {
        if (id == null) {
            throw new RestaurantDomainException("Product ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new RestaurantDomainException("Product name cannot be null or empty");
        }
        if (price == null || !price.isGreaterThanZero()) {
            throw new RestaurantDomainException("Product price must be greater than zero");
        }
    }

    public Product(ProductId id, String name, Money price) {
        this(id, name, price, true);
    }

    public Product updatePrice(Money newPrice) {
        if (newPrice == null || !newPrice.isGreaterThanZero()) {
            throw new RestaurantDomainException("Product price must be greater than zero");
        }
        return new Product(id, name, newPrice, available);
    }

    public Product activate() {
        return new Product(id, name, price, true);
    }

    public Product deactivate() {
        return new Product(id, name, price, false);
    }
}