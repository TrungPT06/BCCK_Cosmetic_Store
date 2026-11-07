package starter.thanhtoan.usecase;

import starter.domain.Money;

import java.time.LocalDate;

/**
 * Pure DTO: Kết quả xử lý thanh toán gửi tới Output Boundary.
 */
public record ThanhToanResponse(
        boolean success,
        String orderId,
        String orderStatusBefore,
        String orderStatusAfter,
        String paymentStatus,
        String message,
        Money refundAmount,
        boolean stockReleased,
        boolean duplicateCallback,
        LocalDate processedDate
) {
    public static ThanhToanResponse failure(String orderId, String status, String message, LocalDate processedDate) {
        return new ThanhToanResponse(false, orderId, status, status, "CHUA_THANH_TOAN", message,
                Money.ZERO, false, false, processedDate);
    }

    public static ThanhToanResponse success(String orderId, String before, String after, String paymentStatus,
                                           Money refund, boolean stockReleased, boolean duplicate, LocalDate processedDate) {
        return new ThanhToanResponse(true, orderId, before, after, paymentStatus, "THANH_CONG",
                refund, stockReleased, duplicate, processedDate);
    }
}
