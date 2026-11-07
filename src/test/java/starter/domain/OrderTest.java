package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.domain.state.DeliveredState;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Aggregate Root Order và chuyển trạng thái")
class OrderTest {

    @Test
    @DisplayName("Kiểm tra hết hạn thanh toán online sau 30 phút")
    void paymentExpiryCheck() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 4, 10, 0, 0);
        Order order = new Order("ORD-1", "an", createdAt,
                OrderStatus.CHO_THANH_TOAN,
                Money.of(700_000L), Money.of(30_000L), "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(700_000L), false)));

        // Chưa quá 30 phút -> false
        assertFalse(order.isPaymentExpired(createdAt.plusMinutes(29)));
        assertFalse(order.isPaymentExpired(createdAt.plusMinutes(30)));

        // Đã quá 30 phút -> true
        assertTrue(order.isPaymentExpired(createdAt.plusMinutes(31)));
    }

    @Test
    @DisplayName("Không thể hủy đơn hàng đã giao (Bug 2)")
    void cannotCancelDeliveredOrder() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 4, 10, 0, 0);
        Order order = new Order("ORD-2", "an", createdAt,
                new DeliveredState(),
                Money.of(700_000L), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(700_000L), false)));

        IllegalStateException ex = assertThrows(IllegalStateException.class, order::cancel);
        assertEquals("KHONG_THE_HUY_DON_DA_GIAO", ex.getMessage());
    }

    @Test
    @DisplayName("Chuyển trạng thái khi thanh toán thành công")
    void markPaidTransitionsToConfirmed() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 4, 10, 0, 0);
        Order order = new Order("ORD-3", "an", createdAt,
                OrderStatus.CHO_THANH_TOAN,
                Money.of(700_000L), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(700_000L), false)));

        order.markPaid();
        assertEquals(OrderStatus.DA_XAC_NHAN, order.orderStatus());
    }
}
