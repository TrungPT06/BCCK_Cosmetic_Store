package starter.thanhtoan.usecase;

import starter.shared.port.OrderRepository;
import starter.domain.Money;
import starter.domain.Order;
import starter.domain.OrderStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Application Use Case (SRP): Xác nhận thu tiền COD cho đơn hàng đã giao.
 * Chỉ cho phép người dùng có vai trò Quản trị viên (admin).
 */
public final class XacNhanThuTienCodUseCase implements ThanhToanInputBoundary {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ThanhToanOutputBoundary outputBoundary;
    private final Clock clock;

    public XacNhanThuTienCodUseCase(OrderRepository orderRepository,
                                    PaymentRepository paymentRepository,
                                    ThanhToanOutputBoundary outputBoundary,
                                    Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository không được null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository không được null");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary không được null");
        this.clock = Objects.requireNonNull(clock, "clock không được null");
    }

    @Override
    public void execute(ThanhToanRequest request) {
        LocalDate today = LocalDate.now(clock);

        var orderOpt = orderRepository.findById(request.orderId());
        if (orderOpt.isEmpty()) {
            outputBoundary.present(ThanhToanResponse.failure(request.orderId(), "KHONG_TON_TAI", "DON_HANG_KHONG_TON_TAI", today));
            return;
        }

        Order order = orderOpt.get();
        String initialStatus = order.status();

        if (!"admin".equalsIgnoreCase(request.role())) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "KHONG_CO_QUYEN_QUAN_TRI", today));
            return;
        }

        if (order.orderStatus() != OrderStatus.DA_GIAO) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "DON_COD_CHUA_GIAO_CHUA_THE_XAC_NHAN_THU_TIEN", today));
            return;
        }

        order.confirmCod();
        orderRepository.save(order);
        paymentRepository.recordTransaction(order.id(), request.action(), "DA_THANH_TOAN");

        outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "DA_GIAO", "DA_THANH_TOAN",
                Money.ZERO, false, false, today));
    }
}
