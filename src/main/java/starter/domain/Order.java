package starter.domain;

import starter.domain.state.OrderState;
import starter.domain.state.OrderStateFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Gốc tổng hợp (Aggregate Root): Đơn hàng mỹ phẩm (Order).
 * Đóng gói toàn bộ các bất biến nghiệp vụ, quy tắc tính toán và trạng thái vòng đời.
 */
public final class Order {
    private static final long ONLINE_PAYMENT_TIMEOUT_MINUTES = 30L;

    private final String id;
    private final String customerId;
    private final LocalDateTime createdAt;
    private OrderState state;
    private final Money totalAmount;
    private final Money shippingFee;
    private final String flag;
    private final List<OrderLine> lines;

    public Order(String id, String customerId, LocalDateTime createdAt, OrderState state,
                 Money totalAmount, Money shippingFee, String flag, List<OrderLine> lines) {
        this.id = Objects.requireNonNull(id, "Order ID không được null");
        this.customerId = Objects.requireNonNull(customerId, "customerId không được null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt không được null");
        this.state = Objects.requireNonNull(state, "OrderState không được null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount không được null");
        this.shippingFee = Objects.requireNonNull(shippingFee, "shippingFee không được null");
        this.flag = flag != null ? flag : "KHONG_CO";
        this.lines = new ArrayList<>(Objects.requireNonNull(lines, "lines không được null"));
    }

    public Order(String id, String customerId, LocalDateTime createdAt, OrderStatus status,
                 Money totalAmount, Money shippingFee, String flag, List<OrderLine> lines) {
        this(id, customerId, createdAt, OrderStateFactory.from(status), totalAmount, shippingFee, flag, lines);
    }

    public String id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public OrderState state() {
        return state;
    }

    public OrderStatus orderStatus() {
        return state.status();
    }

    public String status() {
        return state.status().code();
    }

    public Money total() {
        return totalAmount;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Money shippingFee() {
        return shippingFee;
    }

    public String flag() {
        return flag;
    }

    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Kiểm tra thời hạn thanh toán trực tuyến (30 phút).
     */
    public boolean isPaymentExpired(LocalDateTime now) {
        if (now == null || createdAt == null) {
            return false;
        }
        return Duration.between(createdAt, now).toMinutes() > ONLINE_PAYMENT_TIMEOUT_MINUTES;
    }

    /* --- Chuyển đổi trạng thái nghiệp vụ (State Pattern) --- */
    public void markPaid() {
        this.state = this.state.markPaid();
    }

    public void confirmCod() {
        this.state = this.state.confirmCod();
    }

    public void deliver() {
        this.state = this.state.deliver();
    }

    public void cancel() {
        this.state = this.state.cancel();
    }

    public record OrderLine(Sku sku, int quantity, Money unitPrice, boolean gift) {
        public OrderLine {
            Objects.requireNonNull(sku, "sku không được null");
            Objects.requireNonNull(unitPrice, "unitPrice không được null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }
        }

        public Money subtotal() {
            return Money.of(unitPrice.amount() * quantity);
        }
    }
}
