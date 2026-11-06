package starter.usecase;

import starter.entity.PaymentPolicy;

/**
 * Trường hợp sử dụng ứng dụng (Application Use Case / Control): Xử lý thanh toán đơn hàng.
 *
 * Cải tiến theo Clean Architecture & DDD:
 * - Gửi kết quả qua ThanhToanOutputBoundary.
 * - Kiểm tra chữ ký và số tiền qua cổng thanh toán PaymentGateway.
 * - Ghi nhận lịch sử giao dịch và cập nhật trạng thái đơn hàng kèm orderId.
 */
public final class ThanhToanUseCase implements ThanhToanInputBoundary {
    private final PaymentRepository repo;
    private final PaymentPolicy policy;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ThanhToanOutputBoundary outputBoundary;

    public ThanhToanUseCase(PaymentRepository repo, PaymentPolicy policy,
                            OrderRepository orderRepository,
                            PaymentGateway paymentGateway,
                            ThanhToanOutputBoundary outputBoundary) {
        this.repo = repo;
        this.policy = policy;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway != null ? paymentGateway : PaymentGateway.defaultGateway();
        this.outputBoundary = outputBoundary;
    }

    public ThanhToanUseCase(PaymentRepository repo, PaymentPolicy policy,
                            ThanhToanOutputBoundary outputBoundary) {
        this(repo, policy, null, null, outputBoundary);
    }

    public ThanhToanUseCase(PaymentRepository repo, PaymentPolicy policy) {
        this(repo, policy, null, null, null);
    }

    @Override
    public ThanhToanResponse execute(ThanhToanRequest r) {
        String orderId = (r.userId() != null ? r.userId() : "an") + "_order";

        // Kiểm tra chữ ký và số tiền qua Port PaymentGateway
        if ("THANH_CONG".equals(r.gatewayResult()) && ("THANH_TOAN_ONLINE".equals(r.action()) || "THANH_TOAN_LAI_DOI_PHUONG_THUC".equals(r.action()))) {
            var verification = paymentGateway.verifyCallback(r.signature(), r.gatewayAmount(), r.totalAmount());
            if (!verification.isValidSignature()) {
                return presentResponse(ThanhToanResponse.failure(verification.errorMessage()));
            }
            if (!verification.isAmountMatched()) {
                return presentResponse(ThanhToanResponse.failure(verification.errorMessage()));
            }
        }

        int recordedCount = r.recordedBefore() > 0 ? r.recordedBefore() : repo.transactionCount(orderId);

        var ctx = new PaymentPolicy.PaymentContext(r.role(), r.userId(), r.orderOwner(),
                r.orderStatus(), r.paymentMethod(), r.action(), r.gatewayResult(),
                r.gatewayAmount(), r.signature(), r.totalAmount(), r.minutesSinceCreated(), recordedCount);

        String error = policy.validate(ctx);
        if (error != null) {
            return presentResponse(ThanhToanResponse.failure(error));
        }

        return switch (r.action()) {
            case "THANH_TOAN_ONLINE", "THANH_TOAN_LAI_DOI_PHUONG_THUC" -> handleOnline(r, ctx, orderId);
            case "XAC_NHAN_THU_TIEN_COD" -> handleCod(r, orderId);
            case "HUY_QUA_HAN_THANH_TOAN" -> handleTimeout(r, orderId);
            case "HUY_DON" -> handleCancel(r, orderId);
            default -> presentResponse(ThanhToanResponse.failure("HANH_DONG_KHONG_HOP_LE"));
        };
    }

    private ThanhToanResponse handleOnline(ThanhToanRequest r, PaymentPolicy.PaymentContext ctx, String orderId) {
        if (policy.isDuplicateCallback(ctx.recordedBefore())) {
            return presentResponse(new ThanhToanResponse(true, "DA_XAC_NHAN", r.orderStatus(), "DA_THANH_TOAN", 0, false, true));
        }
        if ("THANH_CONG".equals(r.gatewayResult())) {
            repo.updateOrderStatus(orderId, "DA_XAC_NHAN");
            repo.recordTransaction(orderId, r.action(), "DA_THANH_TOAN");
            return presentResponse(new ThanhToanResponse(true, "DA_XAC_NHAN", "DA_XAC_NHAN", "DA_THANH_TOAN", 0, false, false));
        }
        return presentResponse(new ThanhToanResponse(true, "CHO_THANH_TOAN", r.orderStatus(), "CHUA_THANH_TOAN", 0, false, false));
    }

    private ThanhToanResponse handleCod(ThanhToanRequest r, String orderId) {
        repo.recordTransaction(orderId, r.action(), "DA_THANH_TOAN");
        return presentResponse(new ThanhToanResponse(true, "DA_GIAO", r.orderStatus(), "DA_THANH_TOAN", 0, false, false));
    }

    private ThanhToanResponse handleTimeout(ThanhToanRequest r, String orderId) {
        repo.updateOrderStatus(orderId, "DA_HUY");
        repo.releaseStock(orderId);
        return presentResponse(new ThanhToanResponse(true, "DA_HUY", "DA_HUY", "CHUA_THANH_TOAN", 0, true, false));
    }

    private ThanhToanResponse handleCancel(ThanhToanRequest r, String orderId) {
        repo.updateOrderStatus(orderId, "DA_HUY");
        repo.releaseStock(orderId);
        double refund = r.recordedBefore() > 0 ? r.totalAmount() : 0;
        String paymentStatus = r.recordedBefore() > 0 ? "DA_HOAN_TIEN" : "CHUA_THANH_TOAN";
        return presentResponse(new ThanhToanResponse(true, "DA_HUY", "DA_HUY", paymentStatus, refund, true, false));
    }

    private ThanhToanResponse presentResponse(ThanhToanResponse response) {
        if (outputBoundary != null) {
            outputBoundary.present(response);
        }
        return response;
    }
}
