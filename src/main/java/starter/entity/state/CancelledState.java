package starter.entity.state;

import starter.entity.OrderStatus;

public final class CancelledState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DA_HUY;
    }
}
