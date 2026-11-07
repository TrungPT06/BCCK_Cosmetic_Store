package starter.infrastructure;

import starter.domain.Money;
import starter.thanhtoan.usecase.PaymentGateway;

/**
 * Triển khai mặc định của cổng thanh toán trực tuyến.
 */
public final class DefaultPaymentGateway implements PaymentGateway {
    @Override
    public PaymentVerification verifyCallback(String signature, Money amount, Money expectedAmount) {
        if (signature == null || signature.trim().isEmpty() || "KHONG_HOP_LE".equalsIgnoreCase(signature)) {
            return PaymentVerification.invalidSignature();
        }
        if (amount != null && expectedAmount != null && !amount.equals(expectedAmount)) {
            return PaymentVerification.amountMismatch();
        }
        return PaymentVerification.valid();
    }
}
