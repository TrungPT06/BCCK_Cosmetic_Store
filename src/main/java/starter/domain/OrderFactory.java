package starter.domain;

import starter.domain.state.OrderState;
import starter.domain.state.OrderStateFactory;
import starter.domain.strategy.PaymentStrategy;
import starter.domain.strategy.PaymentStrategyFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Factory tạo Aggregate Root Order, bảo vệ tính toàn vẹn và bất biến khi khởi tạo.
 */
public final class OrderFactory {
    private final PaymentStrategyFactory paymentStrategyFactory;

    public OrderFactory(PaymentStrategyFactory paymentStrategyFactory) {
        this.paymentStrategyFactory = Objects.requireNonNull(paymentStrategyFactory, "paymentStrategyFactory không được null");
    }

    public OrderFactory() {
        this(new PaymentStrategyFactory());
    }

    public Order createOrder(String orderId, String customerId, LocalDateTime createdAt,
                             List<Order.OrderLine> lines, Money merchandiseTotal,
                             Money discount, Money shippingFee, String paymentMethod,
                             boolean hasRestrictedProduct) {
        Objects.requireNonNull(orderId, "orderId không được null");
        Objects.requireNonNull(customerId, "customerId không được null");
        Objects.requireNonNull(createdAt, "createdAt không được null");
        Objects.requireNonNull(lines, "lines không được null");
        Objects.requireNonNull(shippingFee, "shippingFee không được null");

        Money finalMerchandise = merchandiseTotal.minus(discount != null ? discount : Money.ZERO);
        Money totalAmount = finalMerchandise.plus(shippingFee);

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentMethod);
        String initialStatusCode = strategy.initialOrderStatus();
        OrderState initialState = OrderStateFactory.from(initialStatusCode);

        String flag = hasRestrictedProduct ? "HANG_HAN_CHE" : "KHONG_CO";

        return new Order(orderId, customerId, createdAt, initialState, totalAmount, shippingFee, flag, lines);
    }
}
