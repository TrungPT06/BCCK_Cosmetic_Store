package starter.entity;

import java.time.LocalDate;

/**
 * Chính sách doanh nghiệp (Enterprise Policy) và dịch vụ miền (Domain Service) xác thực đơn hàng mỹ phẩm.
 *
 * Nguyên tắc đơn nhiệm (Single Responsibility): kiểm tra bất biến nghiệp vụ khi tạo đơn hàng
 * và xác định trạng thái vòng đời ban đầu của đơn.
 * Thay đổi khi: quy tắc xác thực đơn hàng hoặc vòng đời trạng thái thay đổi.
 *
 * Các trách nhiệm đã được tách ra:
 * - Phí vận chuyển → {@link ShippingPolicy}
 * - Tính giá → {@link PricingPolicy}
 * - Phân loại sản phẩm → {@link ProductClassification}
 * - Màu hiển thị → {@link starter.adapter.DatHangPresenter}
 */
public final class OrderPolicy {
    private static final long SIX_MONTHS = 6;
    private static final double MAX_COD_THRESHOLD = 3_000_000.0;

    public String validate(OrderDraft draft) {
        if ("guest".equalsIgnoreCase(draft.role())) return "YEU_CAU_DANG_NHAP_HOAC_DANG_KY";
        if (!draft.verified()) return "TAI_KHOAN_CHUA_XAC_THUC";
        if (draft.quantity() <= 0 || "KHONG_CO".equals(draft.sku())) return "GIO_HANG_TRONG";
        if (draft.quantity() > draft.availableStock()) return "VUOT_QUA_TON_KHO_KHA_DUNG";
        if (!"HOP_LE".equals(draft.shippingInfo()))
            return "THIEU_DIA_CHI".equals(draft.shippingInfo()) ? "THIEU_THONG_TIN_GIAO_HANG" : "SO_DIEN_THOAI_KHONG_HOP_LE";
        if (draft.expiryDate() != null) {
            if (!draft.expiryDate().isAfter(draft.today())) return "SAN_PHAM_DA_HET_HAN_SU_DUNG";
            if (draft.expiryDate().isBefore(draft.today().plusMonths(SIX_MONTHS))) return "SAN_PHAM_CAN_HAN_DUOI_6_THANG";
        }
        return validateVoucher(draft);
    }

    private String validateVoucher(OrderDraft draft) {
        String voucher = draft.voucher();
        if ("HETHAN".equals(voucher)) return "VOUCHER_DA_HET_HAN";
        if ("HETLUOT".equals(voucher)) return "VOUCHER_HET_LUOT_SU_DUNG";
        if ("DONLON".equals(voucher)) return "VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU";
        if ("COD".equalsIgnoreCase(draft.paymentMethod()) && draft.subtotal() > MAX_COD_THRESHOLD) {
            return "COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC";
        }
        return null;
    }

    public String determineInitialStatus(String paymentMethod) {
        return "COD".equalsIgnoreCase(paymentMethod) ? "DA_XAC_NHAN" : "CHO_THANH_TOAN";
    }

    public record OrderDraft(String role, boolean verified, String sku, int quantity, double unitPrice,
                             LocalDate expiryDate, int availableStock, String shippingInfo, String voucher,
                             double discount, String paymentMethod, LocalDate today) {
        public double subtotal() { return quantity * unitPrice; }
    }
}
