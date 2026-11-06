package starter.entity.strategy;

import starter.entity.OrderStatus;

/**
 * Strategy Pattern: Chiến lược xử lý các phương thức thanh toán khác nhau.
 */
public interface PaymentStrategy {
    String method();
    OrderStatus determineInitialStatus();
    boolean isCod();
    boolean requiresPrepayment(double subtotal, double maxCodThreshold);
}
