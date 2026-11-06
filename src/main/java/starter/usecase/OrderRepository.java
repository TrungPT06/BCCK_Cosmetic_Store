package starter.usecase;

import starter.entity.Order;

/** Cổng đầu ra (Output Port); việc triển khai SQL/NoSQL có thể thay thế adapter in-memory này. */
public interface OrderRepository {
    boolean saveAndReserve(Order order, String purchasedSku, int quantity, String giftSku);
    int stockOf(String sku);
    boolean containsOrder();
}
