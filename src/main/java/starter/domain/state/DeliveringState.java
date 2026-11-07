package starter.domain.state;

import starter.domain.OrderStatus;

public final class DeliveringState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DANG_GIAO;
    }

    @Override
    public OrderState deliver() {
        return new DeliveredState();
    }

    @Override
    public OrderState cancel() {
        throw new IllegalStateException("KHONG_THE_HUY_DON_DANG_GIAO");
    }
}
