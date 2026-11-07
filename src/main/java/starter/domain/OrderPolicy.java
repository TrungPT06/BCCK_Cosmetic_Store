package starter.domain;

import starter.domain.strategy.PaymentStrategy;
import starter.domain.strategy.PaymentStrategyFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Chính sách doanh nghiệp (Enterprise Policy) thẩm định nghiệp vụ đặt hàng.
 */
public final class OrderPolicy {
    private final PaymentStrategyFactory paymentStrategyFactory;

    public OrderPolicy(PaymentStrategyFactory paymentStrategyFactory) {
        this.paymentStrategyFactory = Objects.requireNonNull(paymentStrategyFactory, "paymentStrategyFactory không được null");
    }

    public OrderPolicy() {
        this(new PaymentStrategyFactory());
    }

    public String validate(OrderValidationContext ctx) {
        if ("guest".equalsIgnoreCase(ctx.role())) {
            return "YEU_CAU_DANG_NHAP_HOAC_DANG_KY";
        }
        if (!ctx.verified()) {
            return "TAI_KHOAN_CHUA_XAC_THUC";
        }
        if (ctx.items() == null || ctx.items().isEmpty()) {
            return "GIO_HANG_TRONG";
        }

        for (ItemValidation item : ctx.items()) {
            if (item.quantity() <= 0 || item.product() == null) {
                return "GIO_HANG_TRONG";
            }
            if (item.quantity() > item.availableStock()) {
                return "VUOT_QUA_TON_KHO_KHA_DUNG";
            }
            if (item.product().isExpired(ctx.today())) {
                return "SAN_PHAM_DA_HET_HAN_SU_DUNG";
            }
            if (item.product().isNearExpiry(ctx.today())) {
                return "SAN_PHAM_CAN_HAN_DUOI_6_THANG";
            }
        }

        if (ctx.shippingInfo() != null) {
            String shippingError = ctx.shippingInfo().validate();
            if (shippingError != null) {
                return shippingError;
            }
        }

        if (ctx.voucher() != null) {
            String voucherError = ctx.voucher().validate(ctx.merchandiseTotal(), ctx.today());
            if (voucherError != null) {
                return voucherError;
            }
        }

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(ctx.paymentMethod());
        if (strategy.isThresholdExceeded(ctx.merchandiseTotal())) {
            return "COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC";
        }

        return null;
    }

    public record ItemValidation(Product product, int quantity, int availableStock) {}

    public record OrderValidationContext(
            String role,
            boolean verified,
            List<ItemValidation> items,
            ShippingInfo shippingInfo,
            Voucher voucher,
            Money merchandiseTotal,
            String paymentMethod,
            LocalDate today
    ) {}
}
