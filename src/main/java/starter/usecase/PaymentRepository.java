package starter.usecase;

/**
 * Output port for payment processing. Implementation in infrastructure layer.
 */
public interface PaymentRepository {
    boolean recordTransaction(String action, String paymentStatus);
    boolean updateOrderStatus(String newStatus);
    boolean releaseStock();
    int transactionCount();
}

