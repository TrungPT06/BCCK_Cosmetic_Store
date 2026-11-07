package starter.infrastructure;

import starter.dathang.usecase.OrderRepository;
import starter.domain.Order;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter In-Memory cho OrderRepository.
 * Tuân thủ ISP: chỉ thực hiện lưu và tìm kiếm đơn hàng theo id.
 */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        if (order != null) {
            orders.put(order.id(), order);
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(orders.get(id));
    }
}
