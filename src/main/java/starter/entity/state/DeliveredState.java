package starter.entity.state;

import starter.entity.OrderStatus;

public final class DeliveredState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DA_GIAO;
    }

    @Override
    public OrderState confirmCod() {
        // Đã giao và xác nhận thu tiền COD, đơn hoàn tất giữ nguyên DA_GIAO
        return this;
    }
}
