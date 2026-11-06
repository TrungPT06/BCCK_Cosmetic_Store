package starter.entity;

/**
 * Chính sách doanh nghiệp (Enterprise Policy) tính phí vận chuyển.
 *
 * Nguyên tắc đơn nhiệm (Single Responsibility): chỉ chịu trách nhiệm tính phí vận chuyển.
 * Thay đổi khi: ngưỡng miễn phí vận chuyển hoặc biểu phí vận chuyển thay đổi.
 */
public final class ShippingPolicy {
    private static final double FREE_SHIPPING_THRESHOLD = 500_000.0;
    private static final double STANDARD_SHIPPING_FEE = 30_000.0;

    public double calculateShippingFee(double subtotal) {
        return subtotal >= FREE_SHIPPING_THRESHOLD ? 0.0 : STANDARD_SHIPPING_FEE;
    }
}

