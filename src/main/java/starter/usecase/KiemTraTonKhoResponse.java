package starter.usecase;

/** Plain response DTO for inventory management actions. */
public record KiemTraTonKhoResponse(boolean success, String message, int availableQty,
                                     String pickedBatches, boolean stored, int batchQtyAfter,
                                     /* warning */
                                     String warningStatus, int daysLeft) {
    public static KiemTraTonKhoResponse failure(String error, int available) {
        return new KiemTraTonKhoResponse(false, error, available, "KHONG_AP_DUNG", false, 0, null, 0);
    }
    public static KiemTraTonKhoResponse checkSuccess(String result, int available, String picked) {
        return new KiemTraTonKhoResponse(true, result, available, picked, false, 0, null, 0);
    }
    public static KiemTraTonKhoResponse reserveSuccess(String result, int available, String picked) {
        return new KiemTraTonKhoResponse(true, result, available, picked, true, 0, null, 0);
    }
    public static KiemTraTonKhoResponse adminSuccess(String action, int qtyAfter) {
        return new KiemTraTonKhoResponse(true, action, 0, "KHONG_AP_DUNG", true, qtyAfter, null, 0);
    }
    public static KiemTraTonKhoResponse adminFailure(String error) {
        return new KiemTraTonKhoResponse(false, error, 0, "KHONG_AP_DUNG", false, 0, null, 0);
    }
    public static KiemTraTonKhoResponse warningResult(String status, int daysLeft) {
        return new KiemTraTonKhoResponse(true, status, 0, "KHONG_AP_DUNG", false, 0, status, daysLeft);
    }
}

