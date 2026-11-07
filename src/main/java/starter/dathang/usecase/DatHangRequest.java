package starter.dathang.usecase;

import java.util.List;

/**
 * Pure DTO: Yêu cầu đặt hàng từ client.
 * Không chứa thông tin giá, hạn dùng, hay ngày xử lý do client gửi lên.
 */
public record DatHangRequest(
        String customerId,
        String role,
        boolean verified,
        List<LineItem> items,
        ShippingData shippingData,
        String voucherCode,
        String paymentMethod,
        String giftSku
) {
    public record LineItem(String sku, int quantity) {}

    public record ShippingData(String recipientName, String phone, String address, String city) {}
}
