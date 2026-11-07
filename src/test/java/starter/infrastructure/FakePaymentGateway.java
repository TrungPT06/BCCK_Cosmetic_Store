package starter.infrastructure;

import starter.domain.Money;
import starter.thanhtoan.usecase.PaymentGateway;

/**
 * Fake implementation của PaymentGateway phục vụ môi trường kiểm thử (Test Double).
 */
public final class FakePaymentGateway implements PaymentGateway {
    @Override
    public PaymentVerification verifyCallback(String signature, Money amount, Money expectedAmount) {
        if ("KHONG_HOP_LE".equalsIgnoreCase(signature) || signature == null) {
            return PaymentVerification.invalidSignature();
        }
        if (amount != null && expectedAmount != null && amount.isGreaterThan(Money.ZERO) && !amount.equals(expectedAmount)) {
            return PaymentVerification.amountMismatch();
        }
        return PaymentVerification.valid();
    }
}
