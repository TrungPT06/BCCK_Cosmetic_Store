package starter.entity.state;

import starter.entity.OrderStatus;

public final class OrderStateFactory {
    private OrderStateFactory() {}

    public static OrderState from(OrderStatus status) {
        return switch (status) {
            case CHO_THANH_TOAN -> new PendingPaymentState();
            case DA_XAC_NHAN -> new ConfirmedState();
            case DANG_GIAO -> new DeliveringState();
            case DA_GIAO -> new DeliveredState();
            case DA_HUY -> new CancelledState();
        };
    }

    public static OrderState from(String statusCode) {
        return from(OrderStatus.fromCode(statusCode));
    }
}
