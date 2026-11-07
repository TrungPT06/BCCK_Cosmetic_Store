package starter.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Thực thể Voucher giảm giá.
 * Đóng gói toàn bộ logic kiểm tra hạn dùng, lượt dùng và giá trị đơn hàng tối thiểu.
 * Không chứa dữ liệu test hard-coded trong main.
 */
public class Voucher {
    public static final Voucher NO_VOUCHER = new Voucher("KHONG_DUNG", Money.ZERO, Money.ZERO, LocalDate.MAX, Integer.MAX_VALUE);

    private final String code;
    private final Money discountAmount;
    private final Money minOrderValue;
    private final LocalDate expiryDate;
    private int remainingUses;

    public Voucher(String code, Money discountAmount, Money minOrderValue, LocalDate expiryDate, int remainingUses) {
        this.code = Objects.requireNonNull(code, "Mã voucher không được null");
        this.discountAmount = discountAmount != null ? discountAmount : Money.ZERO;
        this.minOrderValue = minOrderValue != null ? minOrderValue : Money.ZERO;
        this.expiryDate = expiryDate;
        this.remainingUses = remainingUses;
    }

    public static Voucher noVoucher() {
        return NO_VOUCHER;
    }

    public String code() {
        return code;
    }

    public Money discountAmount() {
        return discountAmount;
    }

    public Money minOrderValue() {
        return minOrderValue;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public int remainingUses() {
        return remainingUses;
    }

    public boolean isNoVoucher() {
        return "KHONG_DUNG".equalsIgnoreCase(code) || code.trim().isEmpty();
    }

    /**
     * Xác thực tính hợp lệ của voucher với giá trị đơn hàng và ngày hiện tại.
     * @return null nếu hợp lệ, chuỗi mã lỗi nếu vi phạm điều kiện
     */
    public String validate(Money merchandiseTotal, LocalDate today) {
        if (isNoVoucher()) {
            return null;
        }
        if (expiryDate != null && expiryDate.isBefore(today)) {
            return "VOUCHER_DA_HET_HAN";
        }
        if (remainingUses <= 0) {
            return "VOUCHER_HET_LUOT_SU_DUNG";
        }
        if (minOrderValue != null && merchandiseTotal.isLessThan(minOrderValue)) {
            return "VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU";
        }
        return null;
    }

    public Money calculateDiscount(Money merchandiseTotal) {
        if (isNoVoucher()) {
            return Money.ZERO;
        }
        if (discountAmount.isGreaterThan(merchandiseTotal)) {
            return merchandiseTotal;
        }
        return discountAmount;
    }

    public void decrementUsage() {
        if (remainingUses > 0 && remainingUses != Integer.MAX_VALUE) {
            remainingUses--;
        }
    }
}
