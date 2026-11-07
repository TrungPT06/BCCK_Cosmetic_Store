package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.domain.strategy.PaymentStrategyFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho OrderPolicy")
class OrderPolicyTest {
    private final OrderPolicy policy = new OrderPolicy(new PaymentStrategyFactory());
    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @Test
    @DisplayName("Khách vãng lai (guest) bị từ chối")
    void guestRejected() {
        var ctx = new OrderPolicy.OrderValidationContext(
                "guest", false, Collections.emptyList(), null, null, Money.ZERO, "COD", today
        );
        assertEquals("YEU_CAU_DANG_NHAP_HOAC_DANG_KY", policy.validate(ctx));
    }

    @Test
    @DisplayName("Tài khoản chưa xác thực bị từ chối")
    void unverifiedRejected() {
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", false, Collections.emptyList(), null, null, Money.ZERO, "COD", today
        );
        assertEquals("TAI_KHOAN_CHUA_XAC_THUC", policy.validate(ctx));
    }

    @Test
    @DisplayName("Giỏ hàng rỗng bị từ chối")
    void emptyCartRejected() {
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", true, Collections.emptyList(), null, null, Money.ZERO, "COD", today
        );
        assertEquals("GIO_HANG_TRONG", policy.validate(ctx));
    }

    @Test
    @DisplayName("Vượt tồn kho khả dụng bị từ chối")
    void excessStockRejected() {
        Product p = new Product(Sku.of("SERUM"), "Serum", Money.of(100_000L), today.plusYears(1), false);
        var items = List.of(new OrderPolicy.ItemValidation(p, 5, 3));
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", true, items, null, null, Money.of(500_000L), "COD", today
        );
        assertEquals("VUOT_QUA_TON_KHO_KHA_DUNG", policy.validate(ctx));
    }

    @Test
    @DisplayName("Sản phẩm đã hết hạn bị từ chối")
    void expiredProductRejected() {
        Product p = new Product(Sku.of("SERUM"), "Serum", Money.of(100_000L), today.minusDays(1), false);
        var items = List.of(new OrderPolicy.ItemValidation(p, 1, 10));
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", true, items, null, null, Money.of(100_000L), "COD", today
        );
        assertEquals("SAN_PHAM_DA_HET_HAN_SU_DUNG", policy.validate(ctx));
    }

    @Test
    @DisplayName("Sản phẩm cận hạn dưới 6 tháng bị từ chối")
    void nearExpiryProductRejected() {
        Product p = new Product(Sku.of("SERUM"), "Serum", Money.of(100_000L), today.plusMonths(2), false);
        var items = List.of(new OrderPolicy.ItemValidation(p, 1, 10));
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", true, items, null, null, Money.of(100_000L), "COD", today
        );
        assertEquals("SAN_PHAM_CAN_HAN_DUOI_6_THANG", policy.validate(ctx));
    }

    @Test
    @DisplayName("Đơn hàng COD vượt 3.000.000 VNĐ bị từ chối")
    void codExceedsThresholdRejected() {
        Product p = new Product(Sku.of("SERUM"), "Serum", Money.of(4_500_000L), today.plusYears(1), false);
        var items = List.of(new OrderPolicy.ItemValidation(p, 1, 10));
        var ctx = new OrderPolicy.OrderValidationContext(
                "customer", true, items, new ShippingInfo("An", "0912345678", "123 Đường", "HCM"),
                Voucher.noVoucher(), Money.of(4_500_000L), "COD", today
        );
        assertEquals("COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC", policy.validate(ctx));
    }
}
