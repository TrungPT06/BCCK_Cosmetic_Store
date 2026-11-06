package starter.usecase;

/**
 * Cổng đầu ra (Output Port) cho xử lý thanh toán gắn liền với Order ID.
 */
public interface PaymentRepository {
    boolean recordTransaction(String orderId, String action, String paymentStatus);
    int transactionCount(String orderId);
    boolean updateOrderStatus(String orderId, String newStatus);
    boolean releaseStock(String orderId);

    // Các phương thức tương thích ngược
    default boolean recordTransaction(String action, String paymentStatus) {
        return recordTransaction("default_order", action, paymentStatus);
    }
    default boolean updateOrderStatus(String newStatus) {
        return updateOrderStatus("default_order", newStatus);
    }
    default boolean releaseStock() {
        return releaseStock("default_order");
    }
    default int transactionCount() {
        return transactionCount("default_order");
    }
}
