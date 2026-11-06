package starter.entity.strategy;

import starter.entity.Money;

public final class StandardShippingFeeStrategy implements ShippingFeeStrategy {
    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(500_000.0);
    private static final Money STANDARD_FEE = Money.of(30_000.0);

    @Override
    public Money calculateShippingFee(Money subtotal) {
        if (subtotal.isGreaterThanOrEqualTo(FREE_SHIPPING_THRESHOLD)) {
            return Money.ZERO;
        }
        return STANDARD_FEE;
    }
}
