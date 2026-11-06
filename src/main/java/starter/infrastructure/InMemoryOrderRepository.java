package starter.infrastructure;

import starter.entity.Order;
import starter.usecase.OrderRepository;
import java.util.*;

/**
 * Adapter In-Memory cho Đơn hàng.
 * Đóng vai trò vừa là Kho dữ liệu đơn hàng vừa hỗ trợ kiểm tra assertions trong môi trường test.
 */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Order> ordersById = new LinkedHashMap<>();
    private final List<Order> orders = new ArrayList<>();

    public void setStock(String sku, int quantity) {
        stock.put(sku, quantity);
    }

    @Override
    public void save(Order order) {
        ordersById.put(order.id(), order);
        orders.add(order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    @Override
    public Optional<Order> findLatest() {
        if (orders.isEmpty()) return Optional.empty();
        return Optional.of(orders.get(orders.size() - 1));
    }

    @Override
    public synchronized boolean saveAndReserve(Order order, String sku, int quantity, String giftSku) {
        if (stock.getOrDefault(sku, 0) < quantity || (giftSku != null && stock.getOrDefault(giftSku, 0) < 1)) {
            return false;
        }
        stock.put(sku, stock.get(sku) - quantity);
        if (giftSku != null) {
            stock.put(giftSku, stock.get(giftSku) - 1);
        }
        save(order);
        return true;
    }

    @Override
    public int stockOf(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    /**
     * Phương thức tiện ích chỉ phục vụ assertions trong Test Adapter,
     * không làm ô nhiễm production interface OrderRepository.
     */
    public boolean containsOrder() {
        return !orders.isEmpty();
    }

    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }
}
