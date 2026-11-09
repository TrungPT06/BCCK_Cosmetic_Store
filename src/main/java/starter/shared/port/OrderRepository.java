package starter.shared.port;

import starter.domain.Order;

import java.util.Optional;

/**
 * Port lưu trữ đơn hàng (Interface Segregation Principle - ISP).
 * Chỉ chịu trách nhiệm lưu và truy vấn đơn hàng theo ID.
 */
public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String id);
}
