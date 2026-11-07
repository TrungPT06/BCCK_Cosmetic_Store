package starter.infrastructure;

import starter.dathang.usecase.ProductRepository;
import starter.domain.Product;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter In-Memory cho ProductRepository.
 */
public final class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findBySku(String sku) {
        if (sku == null) return Optional.empty();
        return Optional.ofNullable(products.get(sku));
    }

    @Override
    public void save(Product product) {
        if (product != null) {
            products.put(product.sku().value(), product);
        }
    }
}
