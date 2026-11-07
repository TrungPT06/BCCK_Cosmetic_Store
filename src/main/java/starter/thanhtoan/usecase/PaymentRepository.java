package starter.thanhtoan.usecase;

/**
 * Port ghi nhận và truy vấn nhật ký giao dịch thanh toán (ISP).
 * Không can thiệp cập nhật trạng thái đơn hay nhả tồn kho.
 */
public interface PaymentRepository {
    void recordTransaction(String orderId, String action, String status);

    int transactionCount(String orderId);
}
