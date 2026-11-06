package starter.entity;

import starter.entity.strategy.PaymentStrategyFactory;
import java.time.LocalDate;

/**
 * Chính sách doanh nghiệp (Enterprise Policy) và dịch vụ miền (Domain Service) xác thực đơn hàng mỹ phẩm.
 *
 * Đã loại bỏ các chuỗi magic test-data lọt vào production. Việc xác thực thông tin vận chuyển
 * và voucher được ủy thác cho Value Object ShippingInfo và Entity Voucher với logic thực sự.
 */
public final class OrderPolicy {
    private static final long SIX_MONTHS = 6;
    private static final double MAX_COD_THRESHOLD = 3_000_000.0;

    public String validate(OrderDraft draft) {
        if ("guest".equalsIgnoreCase(draft.role())) return "YEU_CAU_DANG_NHAP_HOAC_DANG_KY";
        if (!draft.verified()) return "TAI_KHOAN_CHUA_XAC_THUC";
        if (draft.quantity() <= 0 || "KHONG_CO".equals(draft.sku())) return "GIO_HANG_TRONG";
        if (draft.quantity() > draft.availableStock()) return "VUOT_QUA_TON_KHO_KHA_DUNG";

        // Xác thực qua Value Object ShippingInfo (logic thật về SĐT và địa chỉ)
        ShippingInfo shipping = draft.shippingInfoObject() != null
                ? draft.shippingInfoObject()
                : ShippingInfo.from(draft.shippingInfo());
        String shippingError = shipping.validate();
        if (shippingError != null) {
            return shippingError;
        }

        if (draft.expiryDate() != null) {
            if (!draft.expiryDate().isAfter(draft.today())) return "SAN_PHAM_DA_HET_HAN_SU_DUNG";
            if (draft.expiryDate().isBefore(draft.today().plusMonths(SIX_MONTHS))) return "SAN_PHAM_CAN_HAN_DUOI_6_THANG";
        }

        // Xác thực qua Entity Voucher (logic thật về hạn dùng, lượt dùng, giá trị tối thiểu)
        Voucher voucher = draft.voucherObject() != null
                ? draft.voucherObject()
                : Voucher.from(draft.voucher(), draft.discount(), draft.today());
        String voucherError = voucher.validate(Money.of(draft.subtotal()), draft.today());
        if (voucherError != null) {
            return voucherError;
        }

        // Ràng buộc thanh toán COD
        var paymentStrategy = PaymentStrategyFactory.from(draft.paymentMethod());
        if (paymentStrategy.requiresPrepayment(draft.subtotal(), MAX_COD_THRESHOLD)) {
            return "COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC";
        }

        return null;
    }

    public String determineInitialStatus(String paymentMethod) {
        return PaymentStrategyFactory.from(paymentMethod).determineInitialStatus().code();
    }

    public record OrderDraft(String role, boolean verified, String sku, int quantity, double unitPrice,
                             LocalDate expiryDate, int availableStock, String shippingInfo, String voucher,
                             double discount, String paymentMethod, LocalDate today,
                             ShippingInfo shippingInfoObject, Voucher voucherObject) {

        public OrderDraft(String role, boolean verified, String sku, int quantity, double unitPrice,
                          LocalDate expiryDate, int availableStock, String shippingInfo, String voucher,
                          double discount, String paymentMethod, LocalDate today) {
            this(role, verified, sku, quantity, unitPrice, expiryDate, availableStock,
                    shippingInfo, voucher, discount, paymentMethod, today, null, null);
        }

        public double subtotal() { return quantity * unitPrice; }
    }
}
