package starter.dathang.usecase;

import starter.domain.Product;

import java.util.Optional;

/**
 * Port truy vấn và quản lý thông tin sản phẩm (Single Source of Truth cho giá và hạn sử dụng).
 */
public interface ProductRepository {
    Optional<Product> findBySku(String sku);

    void save(Product product);
}
