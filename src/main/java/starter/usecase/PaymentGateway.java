package starter.usecase;

/**
 * Cổng thanh toán (Output Port - PaymentGateway).
 * Đóng gói quy tắc xác thực chữ ký callback và giao tiếp với các cổng VNPAY / MOMO.
 */
public interface PaymentGateway {
    record VerificationResult(boolean isValidSignature, boolean isAmountMatched, String errorMessage) {
        public static VerificationResult valid() {
            return new VerificationResult(true, true, null);
        }
        public static VerificationResult invalidSignature() {
            return new VerificationResult(false, false, "CHU_KY_CALLBACK_KHONG_HOP_LE");
        }
        public static VerificationResult amountMismatch() {
            return new VerificationResult(true, false, "SO_TIEN_KHONG_KHOP_VOI_DON");
        }
    }

    VerificationResult verifyCallback(String signature, double callbackAmount, double orderTotal);

    /**
     * Cung cấp một triển khai mặc định (Null Object / In-memory Gateway) ngay tại Port
     * để Use Case không bao giờ phải import tầng hạ tầng (The Dependency Rule).
     */
    static PaymentGateway defaultGateway() {
        return (signature, callbackAmount, orderTotal) -> {
            if ("KHONG_HOP_LE".equalsIgnoreCase(signature)) {
                return VerificationResult.invalidSignature();
            }
            if (callbackAmount > 0 && Math.abs(callbackAmount - orderTotal) > 0.01) {
                return VerificationResult.amountMismatch();
            }
            return VerificationResult.valid();
        };
    }
}
