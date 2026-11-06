package starter.entity.strategy;

public final class PaymentStrategyFactory {
    private PaymentStrategyFactory() {}

    public static PaymentStrategy from(String method) {
        if ("COD".equalsIgnoreCase(method)) {
            return new CodPaymentStrategy();
        }
        return new OnlinePaymentStrategy(method != null ? method.toUpperCase() : "ONLINE");
    }
}
