package starter.usecase;

import starter.entity.Product;
import starter.entity.Sku;
import java.util.Optional;

/**
 * Cổng đầu ra (Output Port) truy xuất thông tin sản phẩm và chính sách giá/hạn dùng từ nguồn dữ liệu chuẩn.
 */
public interface ProductRepository {
    Optional<Product> findBySku(Sku sku);
    default Optional<Product> findBySku(String skuCode) {
        return findBySku(Sku.of(skuCode));
    }
    void save(Product product);
}
