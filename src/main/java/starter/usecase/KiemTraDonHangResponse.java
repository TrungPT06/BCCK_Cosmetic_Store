package starter.usecase;

/** Plain response DTO for order management actions. */
public record KiemTraDonHangResponse(boolean success, String message, String newStatus,
                                      double refundAmount, boolean stockReleased, boolean statusChanged,
                                      boolean stored, int matchedCount, int totalPages) {
    public static KiemTraDonHangResponse failure(String error) {
        return new KiemTraDonHangResponse(false, error, null, 0, false, false, false, 0, 0);
    }
    public static KiemTraDonHangResponse actionSuccess(String newStatus, double refund, boolean stockReleased, boolean statusChanged) {
        return new KiemTraDonHangResponse(true, newStatus, newStatus, refund, stockReleased, statusChanged, true, 0, 0);
    }
    public static KiemTraDonHangResponse viewSuccess(String currentStatus) {
        return new KiemTraDonHangResponse(true, currentStatus, currentStatus, 0, false, false, false, 0, 0);
    }
    public static KiemTraDonHangResponse searchSuccess(int matched, int pages) {
        return new KiemTraDonHangResponse(true, "TIM_KIEM_THANH_CONG", null, 0, false, false, false, matched, pages);
    }
}

