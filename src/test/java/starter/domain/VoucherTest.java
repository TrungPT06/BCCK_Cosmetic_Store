package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Thực thể Voucher (không hard-code test data trong main)")
class VoucherTest {

    @Test
    @DisplayName("Voucher hợp lệ áp dụng thành công và tính mức giảm chính xác")
    void validVoucherDiscount() {
        LocalDate today = LocalDate.of(2026, 11, 4);
        Voucher voucher = new Voucher("GIAM50K", Money.of(50_000L), Money.of(300_000L), today.plusMonths(1), 5);

        assertNull(voucher.validate(Money.of(400_000L), today));
        assertEquals(Money.of(50_000L), voucher.calculateDiscount(Money.of(400_000L)));
    }

    @Test
    @DisplayName("Voucher hết hạn trả về lỗi VOUCHER_DA_HET_HAN")
    void expiredVoucher() {
        LocalDate today = LocalDate.of(2026, 11, 4);
        Voucher voucher = new Voucher("HETHAN", Money.of(50_000L), Money.ZERO, today.minusDays(1), 5);

        assertEquals("VOUCHER_DA_HET_HAN", voucher.validate(Money.of(400_000L), today));
    }

    @Test
    @DisplayName("Voucher hết lượt dùng trả về lỗi VOUCHER_HET_LUOT_SU_DUNG")
    void outOfUsesVoucher() {
        LocalDate today = LocalDate.of(2026, 11, 4);
        Voucher voucher = new Voucher("HETLUOT", Money.of(50_000L), Money.ZERO, today.plusMonths(1), 0);

        assertEquals("VOUCHER_HET_LUOT_SU_DUNG", voucher.validate(Money.of(400_000L), today));
    }

    @Test
    @DisplayName("Voucher chưa đạt giá trị tối thiểu trả về lỗi VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU")
    void minOrderValueNotMet() {
        LocalDate today = LocalDate.of(2026, 11, 4);
        Voucher voucher = new Voucher("DONLON", Money.of(500_000L), Money.of(10_000_000L), today.plusMonths(1), 5);

        assertEquals("VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU", voucher.validate(Money.of(400_000L), today));
    }

    @Test
    @DisplayName("Giảm lượt dùng (decrementUsage) giảm số lần còn lại và hết lượt khi về 0")
    void decrementUsageDecreasesRemainingUses() {
        LocalDate today = LocalDate.of(2026, 11, 4);
        Voucher voucher = new Voucher("GIAM50K", Money.of(50_000L), Money.ZERO, today.plusMonths(1), 1);

        assertEquals(1, voucher.remainingUses());
        assertNull(voucher.validate(Money.of(200_000L), today));

        voucher.decrementUsage();
        assertEquals(0, voucher.remainingUses());
        assertEquals("VOUCHER_HET_LUOT_SU_DUNG", voucher.validate(Money.of(200_000L), today));
    }
}
