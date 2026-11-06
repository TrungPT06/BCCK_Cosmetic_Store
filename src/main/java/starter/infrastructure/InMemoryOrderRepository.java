package starter.infrastructure;

import starter.entity.Order;
import starter.usecase.OrderRepository;
import java.util.*;

/** Test/dev adapter. Its synchronized commit models the atomic inventory-and-order transaction. */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Integer> stock = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();
    public void setStock(String sku, int quantity) { stock.put(sku, quantity); }
    @Override public synchronized boolean saveAndReserve(Order order, String sku, int quantity, String giftSku) {
        if (stock.getOrDefault(sku, 0) < quantity || (giftSku != null && stock.getOrDefault(giftSku, 0) < 1)) return false;
        stock.put(sku, stock.get(sku) - quantity);
        if (giftSku != null) stock.put(giftSku, stock.get(giftSku) - 1);
        orders.add(order); return true;
    }
    @Override public int stockOf(String sku) { return stock.getOrDefault(sku, 0); }
    @Override public boolean containsOrder() { return !orders.isEmpty(); }
}
