package starter.entity;

/**
 * Enterprise policy for shipping fee calculation.
 *
 * Single Responsibility: chỉ chịu trách nhiệm tính phí vận chuyển.
 * Thay đổi khi: ngưỡng miễn phí ship hoặc biểu phí ship thay đổi.
 */
public final class ShippingPolicy {
    private static final double FREE_SHIPPING_THRESHOLD = 500_000.0;
    private static final double STANDARD_SHIPPING_FEE = 30_000.0;

    public double calculateShippingFee(double subtotal) {
        return subtotal >= FREE_SHIPPING_THRESHOLD ? 0.0 : STANDARD_SHIPPING_FEE;
    }
}

