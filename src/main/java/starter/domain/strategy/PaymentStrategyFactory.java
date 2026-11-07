package starter.domain.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory cung cấp PaymentStrategy tuân thủ Open/Closed Principle (OCP).
 * Sử dụng bảng tra Map<String, PaymentStrategy> thay vì chuỗi if-else/switch.
 */
public final class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory() {
        this(defaultStrategies());
    }

    public PaymentStrategyFactory(Map<String, PaymentStrategy> customStrategies) {
        this.strategies = new HashMap<>(Objects.requireNonNull(customStrategies, "strategies không được null"));
    }

    private static Map<String, PaymentStrategy> defaultStrategies() {
        Map<String, PaymentStrategy> map = new HashMap<>();
        map.put("COD", new CodPaymentStrategy());
        map.put("VNPAY", new OnlinePaymentStrategy("VNPAY"));
        map.put("MOMO", new OnlinePaymentStrategy("MOMO"));
        return map;
    }

    public void registerStrategy(String method, PaymentStrategy strategy) {
        strategies.put(Objects.requireNonNull(method), Objects.requireNonNull(strategy));
    }

    public PaymentStrategy getStrategy(String method) {
        if (method == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được null");
        }
        PaymentStrategy strategy = strategies.get(method.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + method);
        }
        return strategy;
    }

    public Map<String, PaymentStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategies);
    }
}
