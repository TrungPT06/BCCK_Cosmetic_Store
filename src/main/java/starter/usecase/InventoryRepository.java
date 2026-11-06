package starter.usecase;

import starter.entity.Sku;

/**
 * Cổng đầu ra (Output Port) quản lý tồn kho độc lập với Đơn hàng.
 */
public interface InventoryRepository {
    int stockOf(Sku sku);
    default int stockOf(String skuCode) {
        return stockOf(Sku.of(skuCode));
    }
    boolean reserve(Sku sku, int quantity);
    default boolean reserve(String skuCode, int quantity) {
        return reserve(Sku.of(skuCode), quantity);
    }
    void release(Sku sku, int quantity);
    default void release(String skuCode, int quantity) {
        release(Sku.of(skuCode), quantity);
    }
    void setStock(Sku sku, int quantity);
    default void setStock(String skuCode, int quantity) {
        setStock(Sku.of(skuCode), quantity);
    }
}
