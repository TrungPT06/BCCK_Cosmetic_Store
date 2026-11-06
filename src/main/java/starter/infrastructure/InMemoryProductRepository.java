package starter.infrastructure;

import starter.entity.Product;
import starter.entity.Sku;
import starter.usecase.ProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new HashMap<>();

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return Optional.ofNullable(products.get(sku.value()));
    }

    @Override
    public void save(Product product) {
        products.put(product.sku().value(), product);
    }
}
