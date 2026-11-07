package starter.domain.strategy;

import starter.domain.Money;

public final class CodPaymentStrategy implements PaymentStrategy {
    private static final Money COD_MAX_THRESHOLD = Money.of(3_000_000L);

    @Override
    public String paymentMethod() {
        return "COD";
    }

    @Override
    public String initialOrderStatus() {
        return "DA_XAC_NHAN";
    }

    @Override
    public boolean isThresholdExceeded(Money merchandiseTotal) {
        return merchandiseTotal.isGreaterThan(COD_MAX_THRESHOLD);
    }
}
