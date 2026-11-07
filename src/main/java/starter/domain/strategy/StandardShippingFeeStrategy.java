package starter.domain.strategy;

import starter.domain.Money;

/**
 * Chính sách phí vận chuyển tiêu chuẩn:
 * - Tiền hàng >= 500.000 VNĐ: miễn phí giao hàng (0 VNĐ).
 * - Dưới 500.000 VNĐ: phí giao hàng cố định 30.000 VNĐ.
 */
public final class StandardShippingFeeStrategy implements ShippingFeeStrategy {
    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(500_000L);
    private static final Money STANDARD_FEE = Money.of(30_000L);

    @Override
    public Money calculateShippingFee(Money merchandiseTotal) {
        if (merchandiseTotal.isGreaterThanOrEqualTo(FREE_SHIPPING_THRESHOLD)) {
            return Money.ZERO;
        }
        return STANDARD_FEE;
    }
}
