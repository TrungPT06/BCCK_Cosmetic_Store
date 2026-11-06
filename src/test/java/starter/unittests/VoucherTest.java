package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.Money;
import starter.entity.Voucher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Entity Voucher (DDD)")
class VoucherTest {

    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @Test
    @DisplayName("Voucher hợp lệ áp dụng giảm giá chính xác")
    void validVoucher() {
        var voucher = new Voucher("GIAM50K", Money.of(50_000.0), Money.ZERO, today.plusMonths(1), 10);
        assertNull(voucher.validate(Money.of(200_000.0), today));
        assertEquals(Money.of(50_000.0), voucher.calculateDiscount(Money.of(200_000.0)));
    }

    @Test
    @DisplayName("Voucher hết hạn sử dụng báo lỗi VOUCHER_DA_HET_HAN")
    void expiredVoucher() {
        var voucher = new Voucher("HETHAN", Money.of(50_000.0), Money.ZERO, today.minusDays(1), 10);
        assertEquals("VOUCHER_DA_HET_HAN", voucher.validate(Money.of(200_000.0), today));
    }

    @Test
    @DisplayName("Voucher hết lượt sử dụng báo lỗi VOUCHER_HET_LUOT_SU_DUNG")
    void outOfUsesVoucher() {
        var voucher = new Voucher("HETLUOT", Money.of(50_000.0), Money.ZERO, today.plusMonths(1), 0);
        assertEquals("VOUCHER_HET_LUOT_SU_DUNG", voucher.validate(Money.of(200_000.0), today));
    }

    @Test
    @DisplayName("Đơn hàng chưa đạt giá trị tối thiểu báo lỗi VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU")
    void belowMinOrderValue() {
        var voucher = new Voucher("DONLON", Money.of(50_000.0), Money.of(1_000_000.0), today.plusMonths(1), 5);
        assertEquals("VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU", voucher.validate(Money.of(500_000.0), today));
    }

    @Test
    @DisplayName("Giảm giá tối đa bằng giá trị tiền hàng, không làm âm")
    void discountCapsAtSubtotal() {
        var voucher = new Voucher("BIG", Money.of(500_000.0), Money.ZERO, today.plusMonths(1), 5);
        assertEquals(Money.of(300_000.0), voucher.calculateDiscount(Money.of(300_000.0)));
    }
}
