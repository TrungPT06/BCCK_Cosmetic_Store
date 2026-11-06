package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.ShippingPolicy;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho ShippingPolicy (Enterprise Business Rules)")
class ShippingPolicyTest {

    private final ShippingPolicy policy = new ShippingPolicy();

    @Test
    @DisplayName("Miễn phí vận chuyển khi tiền hàng đạt đúng 500.000đ")
    void waivesShippingAtExactThreshold() {
        assertEquals(0.0, policy.calculateShippingFee(500_000));
    }

    @Test
    @DisplayName("Miễn phí vận chuyển khi tiền hàng trên 500.000đ")
    void waivesShippingAboveThreshold() {
        assertEquals(0.0, policy.calculateShippingFee(1_000_000));
    }

    @Test
    @DisplayName("Tính phí vận chuyển 30.000đ khi tiền hàng dưới 500.000đ")
    void chargesStandardFeeBelowThreshold() {
        assertEquals(30_000.0, policy.calculateShippingFee(499_000));
        assertEquals(30_000.0, policy.calculateShippingFee(100_000));
    }
}

