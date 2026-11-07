package starter.domain.strategy;

import starter.domain.Money;

/**
 * Strategy Pattern tính phí vận chuyển theo giá trị đơn hàng.
 */
public interface ShippingFeeStrategy {
    Money calculateShippingFee(Money merchandiseTotal);
}
