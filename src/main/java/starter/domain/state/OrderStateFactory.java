package starter.domain.state;

import starter.domain.OrderStatus;

public final class OrderStateFactory {
    private OrderStateFactory() {}

    public static OrderState from(OrderStatus status) {
        if (status == null) {
            return new PendingPaymentState();
        }
        return switch (status) {
            case CHO_THANH_TOAN -> new PendingPaymentState();
            case DA_XAC_NHAN -> new ConfirmedState();
            case DANG_GIAO -> new DeliveringState();
            case DA_GIAO -> new DeliveredState();
            case DA_HUY -> new CancelledState();
        };
    }

    public static OrderState from(String status) {
        if (status == null) {
            return new PendingPaymentState();
        }
        return from(OrderStatus.valueOf(status));
    }
}
