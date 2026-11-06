package starter.usecase;

/**
 * Cổng đầu ra (Output Port) cho xử lý thanh toán. Triển khai nằm ở tầng hạ tầng (Infrastructure).
 */
public interface PaymentRepository {
    boolean recordTransaction(String action, String paymentStatus);
    boolean updateOrderStatus(String newStatus);
    boolean releaseStock();
    int transactionCount();
}

