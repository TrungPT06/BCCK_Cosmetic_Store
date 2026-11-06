package starter.usecase;

import starter.entity.CartLine;
import starter.entity.ShippingInfo;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO yêu cầu đặt hàng (Boundary Request Model).
 * Hỗ trợ cả danh sách sản phẩm trong giỏ hàng (CartLine list)
 * và tương thích ngược với các kịch bản đơn lẻ.
 */
public record DatHangRequest(
        String role,
        boolean verified,
        String sku,
        int quantity,
        double unitPrice,
        LocalDate expiryDate,
        String shippingInfo,
        String voucher,
        double discount,
        String paymentMethod,
        LocalDate today,
        String giftSku,
        List<CartLine> cartLines,
        ShippingInfo shippingInfoObj
) {
    public DatHangRequest(String role, boolean verified, String sku, int quantity, double unitPrice,
                          LocalDate expiryDate, String shippingInfo, String voucher,
                          double discount, String paymentMethod, LocalDate today,
                          String giftSku) {
        this(role, verified, sku, quantity, unitPrice, expiryDate, shippingInfo, voucher, discount,
                paymentMethod, today, giftSku,
                (sku != null && !"KHONG_CO".equals(sku)) ? List.of(CartLine.of(sku, quantity)) : List.of(),
                ShippingInfo.from(shippingInfo));
    }
}
