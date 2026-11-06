package starter.usecase;

import starter.entity.Order;
import starter.entity.OrderPolicy;
import starter.entity.ShippingPolicy;
import starter.entity.PricingPolicy;
import starter.entity.ProductClassification;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trường hợp sử dụng ứng dụng (Application Use Case): đặt hàng mỹ phẩm.
 *
 * Điều phối luồng nghiệp vụ bằng cách phối hợp các chính sách (policy) chuyên biệt (SRP):
 * - OrderPolicy: kiểm tra bất biến nghiệp vụ
 * - ShippingPolicy: tính phí vận chuyển
 * - PricingPolicy: tính tiền hàng sau giảm giá
 * - ProductClassification: phân loại sản phẩm
 *
 * Tồn kho được truy vấn trực tiếp từ Repository (Single Source of Truth),
 * không nhận từ request bên ngoài.
 */
public final class DatHangUseCase implements DatHangInputBoundary {
    private final OrderRepository orders;
    private final OrderPolicy policy;
    private final ShippingPolicy shippingPolicy;
    private final PricingPolicy pricingPolicy;
    private final ProductClassification productClassification;

    public DatHangUseCase(OrderRepository orders, OrderPolicy policy,
                          ShippingPolicy shippingPolicy, PricingPolicy pricingPolicy,
                          ProductClassification productClassification) {
        this.orders = orders;
        this.policy = policy;
        this.shippingPolicy = shippingPolicy;
        this.pricingPolicy = pricingPolicy;
        this.productClassification = productClassification;
    }

    @Override
    public DatHangResponse execute(DatHangRequest r) {
        // Truy vấn tồn kho từ Repository — nguồn dữ liệu chân lý duy nhất (Single Source of Truth)
        int currentStock = orders.stockOf(r.sku());
        int currentGiftStock = hasGift(r.giftSku()) ? orders.stockOf(r.giftSku()) : 0;

        var draft = new OrderPolicy.OrderDraft(r.role(), r.verified(), r.sku(), r.quantity(), r.unitPrice(),
                r.expiryDate(), currentStock, r.shippingInfo(), r.voucher(), r.discount(), r.paymentMethod(), r.today());

        String error = policy.validate(draft);
        if (error != null) {
            return DatHangResponse.failure(error);
        }

        double merchandise = pricingPolicy.calculateMerchandiseTotal(draft.subtotal(), r.discount());
        double shipping = shippingPolicy.calculateShippingFee(draft.subtotal());
        String status = policy.determineInitialStatus(r.paymentMethod());
        String flag = productClassification.determineFlag(r.sku());

        List<Order.OrderLine> lines = new ArrayList<>();
        lines.add(new Order.OrderLine(r.sku(), r.quantity(), r.unitPrice(), false));

        boolean addGift = hasGift(r.giftSku()) && currentGiftStock > 0;
        if (addGift) {
            lines.add(new Order.OrderLine(r.giftSku(), 1, 0.0, true));
        }

        Order order = new Order(UUID.randomUUID().toString(), status, merchandise + shipping, shipping, flag, lines);
        if (!orders.saveAndReserve(order, r.sku(), r.quantity(), addGift ? r.giftSku() : null)) {
            return DatHangResponse.failure("KHONG_THE_LUU_DON_HANG");
        }

        return new DatHangResponse(true, status, status, order.totalAmount(), shipping, orders.stockOf(r.sku()),
                true, flag, 0.0, addGift ? orders.stockOf(r.giftSku()) : currentGiftStock);
    }

    private boolean hasGift(String giftSku) {
        return giftSku != null && !"KHONG_CO".equals(giftSku);
    }
}
