package starter.domain.strategy;

import starter.domain.Money;

public final class OnlinePaymentStrategy implements PaymentStrategy {
    private final String method;

    public OnlinePaymentStrategy(String method) {
        this.method = method;
    }

    @Override
    public String paymentMethod() {
        return method;
    }

    @Override
    public String initialOrderStatus() {
        return "CHO_THANH_TOAN";
    }

    @Override
    public boolean isThresholdExceeded(Money merchandiseTotal) {
        return false;
    }
}
