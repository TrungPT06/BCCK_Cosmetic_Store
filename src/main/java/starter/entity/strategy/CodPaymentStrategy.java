package starter.entity.strategy;

import starter.entity.OrderStatus;

public final class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public String method() {
        return "COD";
    }

    @Override
    public OrderStatus determineInitialStatus() {
        return OrderStatus.DA_XAC_NHAN;
    }

    @Override
    public boolean isCod() {
        return true;
    }

    @Override
    public boolean requiresPrepayment(double subtotal, double maxCodThreshold) {
        return subtotal > maxCodThreshold;
    }
}
