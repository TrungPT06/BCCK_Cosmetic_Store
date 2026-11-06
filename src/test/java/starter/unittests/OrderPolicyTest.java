package starter.unittests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.OrderPolicy;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho OrderPolicy (Enterprise Business Rules)")
class OrderPolicyTest {

    private OrderPolicy policy;
    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @BeforeEach
    void setUp() {
        policy = new OrderPolicy();
    }

    private OrderPolicy.OrderDraft createDraft(String role, boolean verified, String sku, int qty, double price,
                                               LocalDate expiry, int stock, String shipping, String voucher,
                                               double discount, String payment) {
        return new OrderPolicy.OrderDraft(role, verified, sku, qty, price, expiry, stock, shipping, voucher, discount, payment, today);
    }

    private OrderPolicy.OrderDraft validDraft() {
        return createDraft("customer", true, "SERUM_VITC_30", 2, 450_000,
                today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
    }

    @Test
    @DisplayName("Đơn hàng hợp lệ phải vượt qua kiểm tra và không có lỗi")
    void validDraftPassesValidation() {
        assertNull(policy.validate(validDraft()));
    }

    @Test
    @DisplayName("Khách vãng lai (guest) không được phép đặt hàng")
    void rejectsGuest() {
        var draft = createDraft("guest", false, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("YEU_CAU_DANG_NHAP_HOAC_DANG_KY", policy.validate(draft));
    }

    @Test
    @DisplayName("Tài khoản chưa xác thực không được phép đặt hàng")
    void rejectsUnverifiedAccount() {
        var draft = createDraft("customer", false, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("TAI_KHOAN_CHUA_XAC_THUC", policy.validate(draft));
    }

    @Test
    @DisplayName("Giỏ hàng rỗng hoặc không có SKU hợp lệ")
    void rejectsEmptyCart() {
        var emptyQty = createDraft("customer", true, "SERUM_VITC_30", 0, 450_000, today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("GIO_HANG_TRONG", policy.validate(emptyQty));

        var noSku = createDraft("customer", true, "KHONG_CO", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("GIO_HANG_TRONG", policy.validate(noSku));
    }

    @Test
    @DisplayName("Số lượng đặt vượt quá tồn kho khả dụng")
    void rejectsQuantityExceedingStock() {
        var draft = createDraft("customer", true, "SERUM_VITC_30", 15, 450_000, today.plusMonths(12), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("VUOT_QUA_TON_KHO_KHA_DUNG", policy.validate(draft));
    }

    @Test
    @DisplayName("Thông tin giao hàng thiếu địa chỉ hoặc sai số điện thoại")
    void rejectsInvalidShippingInfo() {
        var missingAddress = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "THIEU_DIA_CHI", "KHONG_DUNG", 0, "COD");
        assertEquals("THIEU_THONG_TIN_GIAO_HANG", policy.validate(missingAddress));

        var invalidPhone = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "SDT_KHONG_HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("SO_DIEN_THOAI_KHONG_HOP_LE", policy.validate(invalidPhone));
    }

    @Test
    @DisplayName("Mỹ phẩm đã hết hạn sử dụng không được đặt")
    void rejectsExpiredCosmetics() {
        var expired = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.minusDays(1), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("SAN_PHAM_DA_HET_HAN_SU_DUNG", policy.validate(expired));
    }

    @Test
    @DisplayName("Mỹ phẩm cận hạn dưới 6 tháng không được giao")
    void rejectsNearExpiryCosmetics() {
        var nearExpiry = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(5), 10, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("SAN_PHAM_CAN_HAN_DUOI_6_THANG", policy.validate(nearExpiry));
    }

    @Test
    @DisplayName("Kiểm tra các lỗi voucher không hợp lệ")
    void rejectsInvalidVouchers() {
        var expired = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "HETHAN", 0, "VNPAY");
        assertEquals("VOUCHER_DA_HET_HAN", policy.validate(expired));

        var outOfUses = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "HETLUOT", 0, "VNPAY");
        assertEquals("VOUCHER_HET_LUOT_SU_DUNG", policy.validate(outOfUses));

        var minNotMet = createDraft("customer", true, "SERUM_VITC_30", 1, 450_000, today.plusMonths(12), 10, "HOP_LE", "DONLON", 0, "VNPAY");
        assertEquals("VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU", policy.validate(minNotMet));
    }

    @Test
    @DisplayName("Đơn hàng COD vượt quá ngưỡng 3.000.000 phải thanh toán trước")
    void rejectsCodOverThreshold() {
        var draft = createDraft("customer", true, "SERUM_VITC_30", 10, 450_000, today.plusMonths(12), 20, "HOP_LE", "KHONG_DUNG", 0, "COD");
        assertEquals("COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC", policy.validate(draft));
    }

    @Test
    @DisplayName("Xác định trạng thái ban đầu theo phương thức thanh toán")
    void determinesInitialStatus() {
        assertEquals("DA_XAC_NHAN", policy.determineInitialStatus("COD"));
        assertEquals("CHO_THANH_TOAN", policy.determineInitialStatus("VNPAY"));
        assertEquals("CHO_THANH_TOAN", policy.determineInitialStatus("MOMO"));
    }
}
