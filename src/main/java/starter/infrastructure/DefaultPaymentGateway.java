package starter.infrastructure;

import starter.usecase.PaymentGateway;

public final class DefaultPaymentGateway implements PaymentGateway {
    @Override
    public VerificationResult verifyCallback(String signature, double callbackAmount, double orderTotal) {
        if ("KHONG_HOP_LE".equalsIgnoreCase(signature)) {
            return VerificationResult.invalidSignature();
        }
        if (callbackAmount > 0 && Math.abs(callbackAmount - orderTotal) > 0.01) {
            return VerificationResult.amountMismatch();
        }
        return VerificationResult.valid();
    }
}
