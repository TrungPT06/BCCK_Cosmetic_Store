package starter.entity.strategy;

import starter.entity.Money;

/**
 * Strategy Pattern: Chiến lược tính phí vận chuyển cho đơn hàng.
 */
public interface ShippingFeeStrategy {
    Money calculateShippingFee(Money subtotal);
}
