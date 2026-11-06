package starter.entity;

import starter.entity.state.OrderStateFactory;
import starter.entity.strategy.PaymentStrategyFactory;
import starter.entity.strategy.ShippingFeeStrategy;
import starter.entity.strategy.StandardShippingFeeStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory Pattern: Đóng gói toàn bộ quá trình khởi tạo một Order phức tạp.
 * Chịu trách nhiệm phối hợp tính tiền hàng, áp dụng voucher, tính phí ship và gắn quà tặng.
 */
public final class OrderFactory {
    private final ShippingFeeStrategy shippingFeeStrategy;

    public OrderFactory(ShippingFeeStrategy shippingFeeStrategy) {
        this.shippingFeeStrategy = shippingFeeStrategy != null ? shippingFeeStrategy : new StandardShippingFeeStrategy();
    }

    public OrderFactory() {
        this(new StandardShippingFeeStrategy());
    }

    public Order createOrder(String customerId, List<CartLine> cartLines, List<Product> products,
                             ShippingInfo shippingInfo, Voucher voucher, String paymentMethod,
                             Product giftProduct, boolean hasGiftStock) {
        String orderId = UUID.randomUUID().toString();
        List<Order.OrderLine> orderLines = new ArrayList<>();
        Money subtotal = Money.ZERO;
        String flag = "KHONG_CO";

        for (int i = 0; i < cartLines.size(); i++) {
            CartLine line = cartLines.get(i);
            Product product = products.get(i);
            Money lineTotal = product.unitPrice().amount() > 0
                    ? Money.of(product.unitPrice().amount() * line.quantity())
                    : Money.ZERO;
            subtotal = subtotal.plus(lineTotal);
            orderLines.add(new Order.OrderLine(product.sku().value(), line.quantity(), product.unitPrice().amount(), false));

            if ("HANG_HAN_CHE".equals(product.determineOrderFlag())) {
                flag = "HANG_HAN_CHE";
            }
        }

        // Áp dụng voucher
        Money discount = voucher != null ? voucher.calculateDiscount(subtotal) : Money.ZERO;
        Money merchandiseTotal = subtotal.minus(discount);

        // Quà tặng
        if (giftProduct != null && hasGiftStock) {
            orderLines.add(new Order.OrderLine(giftProduct.sku().value(), 1, 0.0, true));
        }

        // Tính phí ship
        Money shippingFee = shippingFeeStrategy.calculateShippingFee(subtotal);
        Money totalAmount = merchandiseTotal.plus(shippingFee);

        // Trạng thái ban đầu
        var initialStatus = PaymentStrategyFactory.from(paymentMethod).determineInitialStatus();
        var initialState = OrderStateFactory.from(initialStatus);

        return Order.place(orderId, customerId, LocalDateTime.now(), initialState, totalAmount, shippingFee, flag, orderLines);
    }
}
