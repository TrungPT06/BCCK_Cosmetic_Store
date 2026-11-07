package starter.thanhtoan.usecase;

import starter.domain.Money;

/**
 * Port giao tiếp với cổng thanh toán điện tử (VNPAY, MoMo).
 * Thẩm định chữ ký số callback và khớp số tiền thanh toán.
 */
public interface PaymentGateway {
    PaymentVerification verifyCallback(String signature, Money amount, Money expectedAmount);

    record PaymentVerification(boolean isValidSignature, boolean isAmountMatched, String errorMessage) {
        public static PaymentVerification valid() {
            return new PaymentVerification(true, true, null);
        }

        public static PaymentVerification invalidSignature() {
            return new PaymentVerification(false, false, "CHU_KY_CALLBACK_KHONG_HOP_LE");
        }

        public static PaymentVerification amountMismatch() {
            return new PaymentVerification(true, false, "SO_TIEN_KHONG_KHOP_VOI_DON");
        }
    }
}
