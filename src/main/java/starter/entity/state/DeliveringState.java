package starter.entity.state;

import starter.entity.OrderStatus;

public final class DeliveringState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DANG_GIAO;
    }

    @Override
    public OrderState deliver() {
        return new DeliveredState();
    }
}
