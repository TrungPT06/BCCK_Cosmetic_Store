package starter.entity;

/**
 * Enterprise policy for merchandise pricing.
 *
 * Single Responsibility: chỉ chịu trách nhiệm tính tiền hàng sau giảm giá.
 * Thay đổi khi: quy tắc tính giá hoặc giới hạn giảm giá thay đổi.
 * Đảm bảo bất biến: tiền hàng không bao giờ âm.
 */
public final class PricingPolicy {
    public double calculateMerchandiseTotal(double subtotal, double discount) {
        return Math.max(0.0, subtotal - discount);
    }
}

