package starter.usecase;

import java.time.LocalDate;
import java.util.List;

/**
 * Output port for order management. Implementation in infrastructure layer.
 */
public interface OrderManagementRepository {
    /** Represents a stored order for management queries. */
    record ManagedOrder(String orderId, String owner, String status, String paymentStatus,
                        double totalAmount, LocalDate createdDate) {}

    boolean updateStatus(String newStatus);
    boolean releaseStock();
    List<ManagedOrder> search(String filterOrderId, String filterCustomer, String filterStatus,
                              String filterDateRangeStart, String filterDateRangeEnd);
    void addHistoryEntry(String oldStatus, String newStatus);
    int orderCount();
}

