package pl.kopytka.restaurant.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.ProductId;
import pl.kopytka.common.domain.valueobject.RestaurantId;
import pl.kopytka.restaurant.domain.exception.RestaurantDomainException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @EmbeddedId
    private RestaurantId id;

    @NotBlank
    private String name;

    private boolean available = true;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Product> products;

    @Version
    private int version;

    public Restaurant(String name) {
        this.id = RestaurantId.newOne();
        this.name = name;
        this.products = new HashSet<>();
    }

    public void activate() {
        this.available = true;
    }

    public void deactivate() {
        this.available = false;
    }

    public void addProduct(String productName, Money price) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new RestaurantDomainException("Product name cannot be null or empty");
        }
        if (price == null || !price.isGreaterThanZero()) {
            throw new RestaurantDomainException("Product price must be greater than zero");
        }

        Product product = new Product(new ProductId(UUID.randomUUID()), productName, price);
        this.products.add(product);
    }

    public void removeProduct(ProductId productId) {
        if (productId == null) {
            throw new RestaurantDomainException("Product ID cannot be null");
        }
        this.products.removeIf(product -> product.id().equals(productId));
    }

    public boolean hasProduct(ProductId productId) {
        return findProductById(productId) != null;
    }

    public Set<Product> getProducts() {
        return Collections.unmodifiableSet(products);
    }

    private Product findProductById(ProductId productId) {
        return products.stream()
                .filter(product -> product.id().equals(productId))
                .findFirst()
                .orElse(null);
    }
}
