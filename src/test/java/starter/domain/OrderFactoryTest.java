package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.domain.strategy.PaymentStrategyFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho OrderFactory")
class OrderFactoryTest {

    @Test
    @DisplayName("Tạo đơn hàng COD thành công với trạng thái DA_XAC_NHAN và tính đúng tổng tiền")
    void createCodOrder() {
        OrderFactory factory = new OrderFactory(new PaymentStrategyFactory());
        List<Order.OrderLine> lines = List.of(
                new Order.OrderLine(Sku.of("SERUM_VITC_30"), 2, Money.of(450_000L), false)
        );

        Order order = factory.createOrder("ORD-1", "an", LocalDateTime.now(), lines,
                Money.of(900_000L), Money.ZERO, Money.ZERO, "COD", false);

        assertEquals("DA_XAC_NHAN", order.status());
        assertEquals(Money.of(900_000L), order.total());
        assertEquals("KHONG_CO", order.flag());
    }

    @Test
    @DisplayName("Tạo đơn hàng có nước hoa tự động gắn cờ HANG_HAN_CHE")
    void createRestrictedOrder() {
        OrderFactory factory = new OrderFactory(new PaymentStrategyFactory());
        List<Order.OrderLine> lines = List.of(
                new Order.OrderLine(Sku.of("NUOC_HOA_ROSE_50"), 1, Money.of(1_200_000L), false)
        );

        Order order = factory.createOrder("ORD-2", "an", LocalDateTime.now(), lines,
                Money.of(1_200_000L), Money.ZERO, Money.ZERO, "COD", true);

        assertEquals("HANG_HAN_CHE", order.flag());
    }
}
