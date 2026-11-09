package starter.infrastructure;

import starter.shared.port.InventoryRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai InMemory cho InventoryRepository.
 */
public final class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @Override
    public int stockOf(String sku) {
        if (sku == null) return 0;
        return stock.getOrDefault(sku, 0);
    }

    @Override
    public synchronized boolean reserve(String sku, int quantity) {
        if (sku == null) return false;
        int current = stockOf(sku);
        if (current < quantity) {
            return false;
        }
        stock.put(sku, current - quantity);
        return true;
    }

    @Override
    public synchronized void release(String sku, int quantity) {
        if (sku == null) return;
        stock.put(sku, stockOf(sku) + quantity);
    }

    public void setStock(String sku, int quantity) {
        if (sku != null) {
            stock.put(sku, quantity);
        }
    }
}
