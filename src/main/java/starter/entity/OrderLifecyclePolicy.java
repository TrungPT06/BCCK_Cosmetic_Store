package starter.entity;

import java.util.Set;

/**
 * Enterprise policy for order lifecycle management.
 *
 * Single Responsibility: validates order management actions (view, cancel, return, transition)
 * and determines state transitions, refunds, and stock release.
 * Thay đổi khi: quy tắc vòng đời đơn hàng, chính sách hủy/trả hàng thay đổi.
 */
public final class OrderLifecyclePolicy {
    private static final Set<String> RETURN_ELIGIBLE_OPEN_SEAL = Set.of("GIAO_SAI_SAN_PHAM", "HU_HONG_KHI_VAN_CHUYEN");

    public String validateAction(OrderActionContext ctx) {
        if ("guest".equalsIgnoreCase(ctx.role())) return "YEU_CAU_DANG_NHAP_HOAC_DANG_KY";
        if (isAdminOnly(ctx.action()) && !"admin".equalsIgnoreCase(ctx.role()))
            return "KHONG_CO_QUYEN_QUAN_TRI";
        if ("customer".equalsIgnoreCase(ctx.role()) && !ctx.userId().equals(ctx.orderOwner()))
            return "DON_KHONG_THUOC_VE_NGUOI_DUNG";

        return switch (ctx.action()) {
            case "XEM_DON_HANG" -> null;
            case "HUY_DON" -> validateCancel(ctx);
            case "YEU_CAU_TRA_HANG" -> validateReturn(ctx);
            case "CHUYEN_TRANG_THAI" -> validateTransition(ctx);
            case "TIM_KIEM_DON_HANG" -> null;
            case "SUA_DONG_HANG" -> "KHONG_DUOC_SUA_DONG_HANG_CUA_DON_DA_XAC_NHAN";
            default -> "HANH_DONG_KHONG_HOP_LE";
        };
    }

    private boolean isAdminOnly(String action) {
        return "CHUYEN_TRANG_THAI".equals(action) || "TIM_KIEM_DON_HANG".equals(action);
    }

    private String validateCancel(OrderActionContext ctx) {
        if ("DANG_GIAO".equals(ctx.currentStatus()) || "DA_GIAO".equals(ctx.currentStatus()))
            return "KHONG_THE_HUY_DON_DA_CHUYEN_SANG_DANG_GIAO";
        if ("admin".equalsIgnoreCase(ctx.role())
                && ("KHONG_CO".equals(ctx.reasonInput()) || "KHONG_AP_DUNG".equals(ctx.reasonInput())))
            return "THIEU_LY_DO_HUY_DON";
        return null;
    }

    private String validateReturn(OrderActionContext ctx) {
        if (ctx.daysSinceDelivered() > ctx.returnWindowDays()) return "QUA_THOI_HAN_DOI_TRA";
        if ("TRA_RIENG_QUA_TANG".equals(ctx.reasonInput())) return "QUA_TANG_KHONG_DUOC_DOI_TRA_RIENG";
        if ("DA_MO_SEAL".equals(ctx.sealStatus()) && !RETURN_ELIGIBLE_OPEN_SEAL.contains(ctx.reasonInput()))
            return "SAN_PHAM_DA_MO_SEAL_KHONG_THUOC_TRUONG_HOP_DUOC_DOI_TRA";
        if ("KHONG_CO".equals(ctx.evidence()) || ctx.evidence() == null)
            return "THIEU_BANG_CHUNG_ANH_VIDEO_MO_HOP";
        return null;
    }

    private String validateTransition(OrderActionContext ctx) {
        if (!isValidTransition(ctx.currentStatus(), ctx.targetStatus()))
            return "CHUYEN_TRANG_THAI_KHONG_HOP_LE";
        if ("DA_GIAO".equals(ctx.targetStatus()) && !"XAC_NHAN_GIAO_THANH_CONG".equals(ctx.evidence()))
            return "CHUA_CO_XAC_NHAN_GIAO_THANH_CONG";
        return null;
    }

    private boolean isValidTransition(String from, String to) {
        return switch (from) {
            case "DA_XAC_NHAN" -> "DANG_CHUAN_BI".equals(to);
            case "DANG_CHUAN_BI" -> "DANG_GIAO".equals(to);
            case "DANG_GIAO" -> "DA_GIAO".equals(to);
            default -> false;
        };
    }

    public String determineNewStatus(String action, String targetStatus) {
        return switch (action) {
            case "HUY_DON" -> "DA_HUY";
            case "YEU_CAU_TRA_HANG" -> "YEU_CAU_TRA_HANG";
            case "CHUYEN_TRANG_THAI" -> targetStatus;
            default -> null;
        };
    }

    public double calculateRefund(String action, String paymentStatus, double totalAmount) {
        if ("HUY_DON".equals(action) && "DA_THANH_TOAN".equals(paymentStatus)) return totalAmount;
        return 0.0;
    }

    public boolean shouldReleaseStock(String action) {
        return "HUY_DON".equals(action);
    }

    public record OrderActionContext(String role, String userId, String orderOwner,
                                     String currentStatus, String paymentStatus, double totalAmount,
                                     String action, String targetStatus,
                                     String reasonInput, String evidence,
                                     int daysSinceDelivered, int returnWindowDays, String sealStatus) {}
}

