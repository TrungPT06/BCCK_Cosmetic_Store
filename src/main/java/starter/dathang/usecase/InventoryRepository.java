package starter.dathang.usecase;

/**
 * Port quản lý tồn kho sản phẩm (Interface Segregation Principle - ISP).
 * Tách biệt rõ ràng với OrderRepository.
 */
public interface InventoryRepository {
    int stockOf(String sku);

    boolean reserve(String sku, int quantity);

    void release(String sku, int quantity);
}
