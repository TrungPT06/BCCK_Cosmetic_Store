package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.OrderStatus;
import starter.entity.state.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho State Pattern vòng đời đơn hàng")
class OrderStateTest {

    @Test
    @DisplayName("Chuyển trạng thái từ Chờ thanh toán sang Đã xác nhận")
    void pendingToConfirmed() {
        OrderState state = new PendingPaymentState();
        assertEquals(OrderStatus.CHO_THANH_TOAN, state.status());

        OrderState next = state.markPaid();
        assertEquals(OrderStatus.DA_XAC_NHAN, next.status());
    }

    @Test
    @DisplayName("Chuyển trạng thái từ Chờ thanh toán sang Đã hủy")
    void pendingToCancelled() {
        OrderState state = new PendingPaymentState();
        OrderState next = state.cancel();
        assertEquals(OrderStatus.DA_HUY, next.status());
    }

    @Test
    @DisplayName("Không thể giao hàng khi đơn chưa được xác nhận")
    void cannotDeliverUnconfirmed() {
        OrderState state = new PendingPaymentState();
        assertThrows(IllegalStateException.class, state::deliver);
    }

    @Test
    @DisplayName("Luồng giao hàng từ Đã xác nhận sang Đang giao và Đã giao")
    void deliveryFlow() {
        OrderState state = new ConfirmedState();
        OrderState delivering = state.ship();
        assertEquals(OrderStatus.DANG_GIAO, delivering.status());

        OrderState delivered = delivering.deliver();
        assertEquals(OrderStatus.DA_GIAO, delivered.status());
    }
}
