package starter.entity.state;

import starter.entity.OrderStatus;

public final class ConfirmedState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DA_XAC_NHAN;
    }

    @Override
    public OrderState ship() {
        return new DeliveringState();
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }
}
