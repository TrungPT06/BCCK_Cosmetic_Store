package starter.entity;

/**
 * Enterprise policy for payment processing.
 *
 * Single Responsibility: validates payment actions (online payment, COD confirmation,
 * timeout cancellation) and determines payment outcomes.
 * Thay đổi khi: quy tắc thanh toán, timeout, idempotency thay đổi.
 */
public final class PaymentPolicy {
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    public String validate(PaymentContext ctx) {
        if ("guest".equalsIgnoreCase(ctx.role())) return "YEU_CAU_DANG_NHAP_HOAC_DANG_KY";
        if ("customer".equalsIgnoreCase(ctx.role()) && !ctx.userId().equals(ctx.orderOwner()))
            return "DON_KHONG_THUOC_VE_NGUOI_DUNG";

        return switch (ctx.action()) {
            case "THANH_TOAN_ONLINE", "THANH_TOAN_LAI_DOI_PHUONG_THUC" -> validateOnlinePayment(ctx);
            case "XAC_NHAN_THU_TIEN_COD" -> validateCodConfirmation(ctx);
            case "HUY_QUA_HAN_THANH_TOAN", "HUY_DON" -> null;
            default -> "HANH_DONG_KHONG_HOP_LE";
        };
    }

    private String validateOnlinePayment(PaymentContext ctx) {
        if (!"CHO_THANH_TOAN".equals(ctx.orderStatus())) {
            if (ctx.recordedBefore() > 0) return null;
            return "DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN";
        }
        if ("THANH_CONG".equals(ctx.gatewayResult())) {
            if ("KHONG_HOP_LE".equals(ctx.signature())) return "CHU_KY_CALLBACK_KHONG_HOP_LE";
            if (ctx.gatewayAmount() > 0 && ctx.gatewayAmount() != ctx.totalAmount())
                return "SO_TIEN_KHONG_KHOP_VOI_DON";
        }
        return null;
    }

    private String validateCodConfirmation(PaymentContext ctx) {
        if (!"admin".equalsIgnoreCase(ctx.role())) return "KHONG_CO_QUYEN_QUAN_TRI";
        if (!"DA_GIAO".equals(ctx.orderStatus())) return "DON_COD_CHUA_GIAO_CHUA_THE_XAC_NHAN_THU_TIEN";
        return null;
    }

    public boolean isPaymentTimeout(int minutesSinceCreated) {
        return minutesSinceCreated > PAYMENT_TIMEOUT_MINUTES;
    }

    public boolean isDuplicateCallback(int recordedBefore) {
        return recordedBefore > 0;
    }

    public record PaymentContext(String role, String userId, String orderOwner,
                                 String orderStatus, String paymentMethod,
                                 String action, String gatewayResult,
                                 double gatewayAmount, String signature,
                                 double totalAmount, int minutesSinceCreated, int recordedBefore) {}
}

