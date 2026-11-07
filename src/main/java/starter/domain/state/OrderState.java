package starter.domain.state;

import starter.domain.OrderStatus;

/**
 * State Pattern: Giao diện trạng thái trong vòng đời đơn hàng.
 * Đóng gói các quy tắc chuyển đổi trạng thái hợp lệ, ngăn chặn vi phạm bất biến.
 */
public interface OrderState {
    OrderStatus status();

    default OrderState markPaid() {
        throw new IllegalStateException("Không thể chuyển sang Đã xác nhận từ trạng thái " + status());
    }

    default OrderState confirmCod() {
        throw new IllegalStateException("Không thể xác nhận thu tiền COD từ trạng thái " + status());
    }

    default OrderState ship() {
        throw new IllegalStateException("Không thể bắt đầu giao hàng từ trạng thái " + status());
    }

    default OrderState deliver() {
        throw new IllegalStateException("Không thể chuyển sang Đã giao từ trạng thái " + status());
    }

    default OrderState cancel() {
        throw new IllegalStateException("Không thể hủy đơn từ trạng thái " + status());
    }
}
