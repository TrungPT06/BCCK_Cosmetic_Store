package starter.infrastructure;

import starter.usecase.PaymentRepository;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryPaymentRepository implements PaymentRepository {
    private String orderStatus;
    private final List<String> transactions = new ArrayList<>();
    private boolean stockReleased;

    public void setOrderStatus(String status) { this.orderStatus = status; }

    @Override public boolean recordTransaction(String action, String paymentStatus) { transactions.add(action + "|" + paymentStatus); return true; }
    @Override public boolean updateOrderStatus(String newStatus) { orderStatus = newStatus; return true; }
    @Override public boolean releaseStock() { stockReleased = true; return true; }
    @Override public int transactionCount() { return transactions.size(); }
    public boolean isStockReleased() { return stockReleased; }
    public String getOrderStatus() { return orderStatus; }
    public boolean hasTransactions() { return !transactions.isEmpty(); }
}

