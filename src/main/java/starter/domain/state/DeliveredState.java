package starter.domain.state;

import starter.domain.OrderStatus;

public final class DeliveredState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DA_GIAO;
    }

    @Override
    public OrderState confirmCod() {
        return this;
    }

    @Override
    public OrderState cancel() {
        throw new IllegalStateException("KHONG_THE_HUY_DON_DA_GIAO");
    }
}
