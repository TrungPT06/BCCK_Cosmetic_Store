package starter.infrastructure;

import starter.entity.Sku;
import starter.usecase.InventoryRepository;

import java.util.HashMap;
import java.util.Map;

public final class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, Integer> stock = new HashMap<>();

    @Override
    public int stockOf(Sku sku) {
        return stock.getOrDefault(sku.value(), 0);
    }

    @Override
    public synchronized boolean reserve(Sku sku, int quantity) {
        int current = stockOf(sku);
        if (current < quantity) {
            return false;
        }
        stock.put(sku.value(), current - quantity);
        return true;
    }

    @Override
    public synchronized void release(Sku sku, int quantity) {
        stock.put(sku.value(), stockOf(sku) + quantity);
    }

    @Override
    public void setStock(Sku sku, int quantity) {
        stock.put(sku.value(), quantity);
    }
}
