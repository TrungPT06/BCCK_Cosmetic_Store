package starter.entity;

import starter.entity.state.OrderState;
import starter.entity.state.OrderStateFactory;

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
        this.customerId = customerId != null ? customerId : "an";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.state = Objects.requireNonNull(state, "OrderState không được null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount không được null");
        this.shippingFee = Objects.requireNonNull(shippingFee, "shippingFee không được null");
        this.flag = flag != null ? flag : "KHONG_CO";
        this.lines = new ArrayList<>(Objects.requireNonNull(lines, "lines không được null"));
    }

    /** Constructor tương thích với các DTO và kịch bản cũ */
    public Order(String id, String status, double totalAmount, double shippingFee,
                 String flag, List<OrderLine> lines) {
        this(id, "an", LocalDateTime.now(), OrderStateFactory.from(status),
                Money.of(totalAmount), Money.of(shippingFee), flag, lines);
    }

    public static Order place(String id, String customerId, LocalDateTime createdAt,
                              OrderState initialState, Money totalAmount, Money shippingFee,
                              String flag, List<OrderLine> lines) {
        return new Order(id, customerId, createdAt, initialState, totalAmount, shippingFee, flag, lines);
    }

    public String id() { return id; }
    public String getId() { return id; }
    public String customerId() { return customerId; }
    public LocalDateTime createdAt() { return createdAt; }
    public OrderState state() { return state; }
    public OrderStatus orderStatus() { return state.status(); }
    public String status() { return state.status().code(); }
    public String getStatus() { return status(); }
    public double totalAmount() { return totalAmount.amount(); }
    public Money total() { return totalAmount; }
    public double shippingFee() { return shippingFee.amount(); }
    public Money shipping() { return shippingFee; }
    public String flag() { return flag; }
    public List<OrderLine> lines() { return Collections.unmodifiableList(lines); }

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

    public record OrderLine(String sku, int quantity, double unitPrice, boolean gift) {
        public Money subtotal() {
            return Money.of(unitPrice * quantity);
        }
    }
}
