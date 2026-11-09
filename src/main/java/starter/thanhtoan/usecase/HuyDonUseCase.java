package starter.thanhtoan.usecase;

import starter.shared.port.InventoryRepository;
import starter.shared.port.OrderRepository;
import starter.domain.Money;
import starter.domain.Order;
import starter.domain.OrderStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application Use Case (SRP): Hủy đơn hàng (Hủy thủ công hoặc Hủy do quá hạn thanh toán 30 phút).
 * Quy tắc:
 * - Hủy quá hạn: chỉ áp dụng cho đơn CHO_THANH_TOAN và đã quá 30 phút, nếu chưa thì CHUA_QUA_HAN_THANH_TOAN.
 * - Không thể hủy đơn đã giao: KHONG_THE_HUY_DON_DA_GIAO.
 */
public final class HuyDonUseCase implements ThanhToanInputBoundary {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final ThanhToanOutputBoundary outputBoundary;
    private final Clock clock;

    public HuyDonUseCase(OrderRepository orderRepository,
                         InventoryRepository inventoryRepository,
                         PaymentRepository paymentRepository,
                         ThanhToanOutputBoundary outputBoundary,
                         Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository không được null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository không được null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository không được null");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary không được null");
        this.clock = Objects.requireNonNull(clock, "clock không được null");
    }

    @Override
    public void execute(ThanhToanRequest request) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        var orderOpt = orderRepository.findById(request.orderId());
        if (orderOpt.isEmpty()) {
            outputBoundary.present(ThanhToanResponse.failure(request.orderId(), "KHONG_TON_TAI", "DON_HANG_KHONG_TON_TAI", today));
            return;
        }

        Order order = orderOpt.get();
        String initialStatus = order.status();

        if ("HUY_QUA_HAN_THANH_TOAN".equals(request.action())) {
            handleTimeoutCancel(order, initialStatus, now, today);
        } else {
            handleManualCancel(request, order, initialStatus, today);
        }
    }

    private void handleTimeoutCancel(Order order, String initialStatus, LocalDateTime now, LocalDate today) {
        // Chỉ đơn đang chờ thanh toán mới được tự động hủy do quá hạn
        if (order.orderStatus() != OrderStatus.CHO_THANH_TOAN) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN", today));
            return;
        }
        // Thẩm định xem đơn đã quá hạn 30 phút chưa
        if (!order.isPaymentExpired(now)) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "CHUA_QUA_HAN_THANH_TOAN", today));
            return;
        }

        try {
            order.cancel();
        } catch (IllegalStateException e) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, e.getMessage(), today));
            return;
        }

        orderRepository.save(order);
        releaseStock(order);

        outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "DA_HUY", "CHUA_THANH_TOAN",
                Money.ZERO, true, false, today));
    }

    private void handleManualCancel(ThanhToanRequest request, Order order, String initialStatus, LocalDate today) {
        if ("customer".equalsIgnoreCase(request.role()) && !Objects.equals(request.userId(), order.customerId())) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "DON_KHONG_THUOC_VE_NGUOI_DUNG", today));
            return;
        }

        // Không thể hủy đơn hàng đã giao
        if (order.orderStatus() == OrderStatus.DA_GIAO) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "KHONG_THE_HUY_DON_DA_GIAO", today));
            return;
        }

        // Chỉ hoàn tiền khi đã có giao dịch thanh toán thành công được ghi nhận (COD chưa thu thì không hoàn)
        boolean paid = paymentRepository.transactionCount(order.id()) > 0;
        try {
            order.cancel();
        } catch (IllegalStateException e) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, e.getMessage(), today));
            return;
        }

        orderRepository.save(order);
        releaseStock(order);

        Money refundAmount = paid ? order.total() : Money.ZERO;
        String paymentStatus = paid ? "DA_HOAN_TIEN" : "CHUA_THANH_TOAN";

        outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "DA_HUY", paymentStatus,
                refundAmount, true, false, today));
    }

    private void releaseStock(Order order) {
        for (Order.OrderLine line : order.lines()) {
            inventoryRepository.release(line.sku().value(), line.quantity());
        }
    }
}
