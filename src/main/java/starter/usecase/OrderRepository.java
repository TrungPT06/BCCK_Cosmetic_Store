package starter.usecase;

import starter.entity.Order;

/** Output port; a SQL/NoSQL implementation can replace the in-memory adapter. */
public interface OrderRepository {
    boolean saveAndReserve(Order order, String purchasedSku, int quantity, String giftSku);
    int stockOf(String sku);
    boolean containsOrder();
}
