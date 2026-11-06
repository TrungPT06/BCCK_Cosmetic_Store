package starter.usecase;

import starter.entity.Order;
import java.util.Optional;

/**
 * Cổng đầu ra (Output Port) lưu trữ và truy xuất Đơn hàng.
 * Đã loại bỏ hoàn toàn các phương thức phục vụ test (containsOrder) để giữ cho domain port trong sạch.
 */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
    Optional<Order> findLatest();

    /**
     * Đồng bộ giao dịch nguyên tử (Atomic Transaction) giữa lưu đơn và giữ chỗ tồn kho
     * khi chưa tách riêng Microservices.
     */
    boolean saveAndReserve(Order order, String purchasedSku, int quantity, String giftSku);
    int stockOf(String sku);
}
