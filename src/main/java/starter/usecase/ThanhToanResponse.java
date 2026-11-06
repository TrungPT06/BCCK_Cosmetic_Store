package starter.usecase;

/** DTO phản hồi thuần túy cho các thao tác thanh toán. */
public record ThanhToanResponse(boolean success, String message,
                                 String orderStatusAfter, String paymentStatus,
                                 double refundAmount, boolean stockReleased,
                                 boolean isDuplicate) {
    public static ThanhToanResponse failure(String error) {
        return new ThanhToanResponse(false, error, null, null, 0, false, false);
    }
}

