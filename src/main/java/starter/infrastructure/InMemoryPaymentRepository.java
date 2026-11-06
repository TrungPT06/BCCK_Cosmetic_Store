package starter.infrastructure;

import starter.usecase.PaymentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryPaymentRepository implements PaymentRepository {
    private String orderStatus;
    private final Map<String, List<String>> transactionsByOrder = new HashMap<>();
    private final List<String> transactions = new ArrayList<>();
    private boolean stockReleased;

    public void setOrderStatus(String status) { this.orderStatus = status; }

    @Override
    public boolean recordTransaction(String orderId, String action, String paymentStatus) {
        String record = action + "|" + paymentStatus;
        transactions.add(record);
        transactionsByOrder.computeIfAbsent(orderId, k -> new ArrayList<>()).add(record);
        return true;
    }

    @Override
    public int transactionCount(String orderId) {
        return transactionsByOrder.getOrDefault(orderId, List.of()).size();
    }

    @Override
    public boolean updateOrderStatus(String orderId, String newStatus) {
        this.orderStatus = newStatus;
        return true;
    }

    @Override
    public boolean releaseStock(String orderId) {
        this.stockReleased = true;
        return true;
    }

    @Override public boolean recordTransaction(String action, String paymentStatus) {
        return recordTransaction("default_order", action, paymentStatus);
    }

    @Override public boolean updateOrderStatus(String newStatus) {
        this.orderStatus = newStatus;
        return true;
    }

    @Override public boolean releaseStock() {
        this.stockReleased = true;
        return true;
    }

    @Override public int transactionCount() {
        return transactions.size();
    }

    public boolean isStockReleased() { return stockReleased; }
    public String getOrderStatus() { return orderStatus; }
    public boolean hasTransactions() { return !transactions.isEmpty(); }
}
