package starter.domain.strategy;

import starter.domain.Money;

/**
 * Strategy Pattern: Chiến lược xử lý cho từng phương thức thanh toán.
 */
public interface PaymentStrategy {
    String paymentMethod();

    String initialOrderStatus();

    boolean isThresholdExceeded(Money merchandiseTotal);
}
