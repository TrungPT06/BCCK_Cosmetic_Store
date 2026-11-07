package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.domain.strategy.PaymentStrategy;
import starter.domain.strategy.PaymentStrategyFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử cho PaymentStrategy và PaymentStrategyFactory (OCP)")
class PaymentStrategyTest {

    @Test
    @DisplayName("PaymentStrategyFactory lấy đúng strategy qua Map, không dùng chuỗi if-else")
    void factoryReturnsCorrectStrategy() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();

        PaymentStrategy cod = factory.getStrategy("COD");
        assertEquals("DA_XAC_NHAN", cod.initialOrderStatus());
        assertTrue(cod.isThresholdExceeded(Money.of(3_500_000L)));
        assertFalse(cod.isThresholdExceeded(Money.of(2_500_000L)));

        PaymentStrategy vnpay = factory.getStrategy("VNPAY");
        assertEquals("CHO_THANH_TOAN", vnpay.initialOrderStatus());
        assertFalse(vnpay.isThresholdExceeded(Money.of(10_000_000L)));

        PaymentStrategy momo = factory.getStrategy("MOMO");
        assertEquals("CHO_THANH_TOAN", momo.initialOrderStatus());
    }

    @Test
    @DisplayName("Phương thức thanh toán không hỗ trợ ném lỗi")
    void unsupportedPaymentMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy("BITCOIN"));
    }
}
