package starter.thanhtoan.usecase;

import starter.shared.port.OrderRepository;
import starter.domain.Money;
import starter.domain.Order;
import starter.domain.OrderStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Application Use Case (SRP): Xử lý thanh toán trực tuyến (VNPAY, MoMo).
 * - Tự động nạp thông tin đơn từ OrderRepository.
 * - Kiểm tra chữ ký và số tiền qua PaymentGateway.
 * - Xử lý tính bất biến lặp lại (idempotency) khi nhận callback trùng lặp.
 */
public final class ThanhToanOnlineUseCase implements ThanhToanInputBoundary {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ThanhToanOutputBoundary outputBoundary;
    private final Clock clock;

    public ThanhToanOnlineUseCase(OrderRepository orderRepository,
                                  PaymentRepository paymentRepository,
                                  PaymentGateway paymentGateway,
                                  ThanhToanOutputBoundary outputBoundary,
                                  Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository không được null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository không được null");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway không được null");
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

        // Xác thực người dùng và quyền truy cập
        if ("guest".equalsIgnoreCase(request.role())) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "YEU_CAU_DANG_NHAP_HOAC_DANG_KY", today));
            return;
        }
        if ("customer".equalsIgnoreCase(request.role()) && !Objects.equals(request.userId(), order.customerId())) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "DON_KHONG_THUOC_VE_NGUOI_DUNG", today));
            return;
        }

        // Kiểm tra callback trùng lặp (Idempotency)
        int txCount = paymentRepository.transactionCount(order.id());
        if (txCount > 0 && order.orderStatus() == OrderStatus.DA_XAC_NHAN) {
            outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "DA_XAC_NHAN", "DA_THANH_TOAN",
                    Money.ZERO, false, true, today));
            return;
        }

        // Kiểm tra trạng thái đơn phải là CHO_THANH_TOAN
        if (order.orderStatus() != OrderStatus.CHO_THANH_TOAN) {
            outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, "DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN", today));
            return;
        }

        // Kiểm tra kết quả từ cổng thanh toán
        if ("THANH_CONG".equals(request.gatewayResult())) {
            var verification = paymentGateway.verifyCallback(request.signature(), request.gatewayAmount(), order.total());
            if (!verification.isValidSignature()) {
                outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, verification.errorMessage(), today));
                return;
            }
            if (!verification.isAmountMatched()) {
                outputBoundary.present(ThanhToanResponse.failure(order.id(), initialStatus, verification.errorMessage(), today));
                return;
            }

            order.markPaid();
            orderRepository.save(order);
            paymentRepository.recordTransaction(order.id(), request.action(), "DA_THANH_TOAN");

            outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "DA_XAC_NHAN", "DA_THANH_TOAN",
                    Money.ZERO, false, false, today));
        } else {
            // Thanh toán online thất bại -> đơn giữ nguyên trạng thái CHO_THANH_TOAN
            outputBoundary.present(ThanhToanResponse.success(order.id(), initialStatus, "CHO_THANH_TOAN", "CHUA_THANH_TOAN",
                    Money.ZERO, false, false, today));
        }
    }
}
