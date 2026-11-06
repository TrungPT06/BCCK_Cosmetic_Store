package starter.entity;

import java.time.LocalDate;

/**
 * Thực thể / Mô hình nghiệp vụ Voucher giảm giá.
 * Đóng gói toàn bộ logic kiểm tra hạn dùng, lượt dùng và giá trị đơn hàng tối thiểu.
 */
public class Voucher {
    public static final Voucher NO_VOUCHER = new Voucher("KHONG_DUNG", Money.ZERO, Money.ZERO, LocalDate.MAX, Integer.MAX_VALUE);

    private final String code;
    private final Money discountAmount;
    private final Money minOrderValue;
    private final LocalDate expiryDate;
    private int remainingUses;

    public Voucher(String code, Money discountAmount, Money minOrderValue, LocalDate expiryDate, int remainingUses) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.expiryDate = expiryDate;
        this.remainingUses = remainingUses;
    }

    public static Voucher noVoucher() {
        return NO_VOUCHER;
    }

    public static Voucher from(String voucherCode, double discount, LocalDate today) {
        if (voucherCode == null || "KHONG_DUNG".equalsIgnoreCase(voucherCode)) {
            return noVoucher();
        }
        if ("HETHAN".equalsIgnoreCase(voucherCode)) {
            return new Voucher("HETHAN", Money.of(discount), Money.ZERO, today.minusDays(1), 10);
        }
        if ("HETLUOT".equalsIgnoreCase(voucherCode)) {
            return new Voucher("HETLUOT", Money.of(discount), Money.ZERO, today.plusMonths(1), 0);
        }
        if ("DONLON".equalsIgnoreCase(voucherCode)) {
            return new Voucher("DONLON", Money.of(discount), Money.of(10_000_000.0), today.plusMonths(1), 10);
        }
        return new Voucher(voucherCode, Money.of(discount), Money.ZERO, today.plusMonths(6), 10);
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
        return "KHONG_DUNG".equalsIgnoreCase(code) || code == null || code.trim().isEmpty();
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
        // Giảm tối đa bằng giá trị tiền hàng, không làm âm tiền
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
