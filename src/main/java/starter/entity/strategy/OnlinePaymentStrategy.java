package starter.entity.strategy;

import starter.entity.OrderStatus;

public final class OnlinePaymentStrategy implements PaymentStrategy {
    private final String method;

    public OnlinePaymentStrategy(String method) {
        this.method = method;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public OrderStatus determineInitialStatus() {
        return OrderStatus.CHO_THANH_TOAN;
    }

    @Override
    public boolean isCod() {
        return false;
    }

    @Override
    public boolean requiresPrepayment(double subtotal, double maxCodThreshold) {
        return false;
    }
}
