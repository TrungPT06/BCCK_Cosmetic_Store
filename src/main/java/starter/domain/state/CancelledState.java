package starter.domain.state;

import starter.domain.OrderStatus;

public final class CancelledState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.DA_HUY;
    }
}
