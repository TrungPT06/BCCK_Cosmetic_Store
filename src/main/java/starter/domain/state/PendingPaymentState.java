package starter.domain.state;

import starter.domain.OrderStatus;

public final class PendingPaymentState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.CHO_THANH_TOAN;
    }

    @Override
    public OrderState markPaid() {
        return new ConfirmedState();
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }
}
