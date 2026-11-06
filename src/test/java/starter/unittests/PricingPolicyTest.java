package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.PricingPolicy;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho PricingPolicy (Enterprise Business Rules)")
class PricingPolicyTest {

    private final PricingPolicy policy = new PricingPolicy();

    @Test
    @DisplayName("Tiền hàng sau giảm giá tính đúng khi giảm giá nhỏ hơn tiền hàng")
    void subtractsDiscountFromSubtotal() {
        assertEquals(400_000.0, policy.calculateMerchandiseTotal(450_000, 50_000));
    }

    @Test
    @DisplayName("Tiền hàng sau giảm giá không bao giờ bị âm khi giảm giá lớn hơn tiền hàng")
    void neverGoesNegative() {
        assertEquals(0.0, policy.calculateMerchandiseTotal(350_000, 500_000));
    }

    @Test
    @DisplayName("Tiền hàng không thay đổi khi không có giảm giá")
    void noDiscountKeepsOriginal() {
        assertEquals(900_000.0, policy.calculateMerchandiseTotal(900_000, 0));
    }
}

