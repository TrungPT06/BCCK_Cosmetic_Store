package starter.infrastructure;

import starter.thanhtoan.usecase.PaymentRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter In-Memory cho PaymentRepository.
 * Tuân thủ ISP: chỉ ghi và đếm số lượng giao dịch thanh toán.
 */
public final class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, List<String>> transactionsByOrder = new ConcurrentHashMap<>();

    @Override
    public void recordTransaction(String orderId, String action, String status) {
        if (orderId == null) return;
        String record = action + "|" + status;
        transactionsByOrder.computeIfAbsent(orderId, k -> Collections.synchronizedList(new ArrayList<>())).add(record);
    }

    @Override
    public int transactionCount(String orderId) {
        if (orderId == null) return 0;
        List<String> list = transactionsByOrder.get(orderId);
        return list != null ? list.size() : 0;
    }
}
