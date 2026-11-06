package starter.usecase;

import starter.entity.PaymentPolicy;

public final class ThanhToanUseCase implements ThanhToanInputBoundary {
    private final PaymentRepository repo;
    private final PaymentPolicy policy;

    public ThanhToanUseCase(PaymentRepository repo, PaymentPolicy policy) {
        this.repo = repo; this.policy = policy;
    }

    @Override
    public ThanhToanResponse execute(ThanhToanRequest r) {
        var ctx = new PaymentPolicy.PaymentContext(r.role(), r.userId(), r.orderOwner(),
                r.orderStatus(), r.paymentMethod(), r.action(), r.gatewayResult(),
                r.gatewayAmount(), r.signature(), r.totalAmount(), r.minutesSinceCreated(), r.recordedBefore());

        String error = policy.validate(ctx);
        if (error != null) return ThanhToanResponse.failure(error);

        return switch (r.action()) {
            case "THANH_TOAN_ONLINE", "THANH_TOAN_LAI_DOI_PHUONG_THUC" -> handleOnline(r, ctx);
            case "XAC_NHAN_THU_TIEN_COD" -> handleCod(r);
            case "HUY_QUA_HAN_THANH_TOAN" -> handleTimeout(r);
            case "HUY_DON" -> handleCancel(r);
            default -> ThanhToanResponse.failure("HANH_DONG_KHONG_HOP_LE");
        };
    }

    private ThanhToanResponse handleOnline(ThanhToanRequest r, PaymentPolicy.PaymentContext ctx) {
        if (policy.isDuplicateCallback(r.recordedBefore())) {
            return new ThanhToanResponse(true, "DA_XAC_NHAN", r.orderStatus(), "DA_THANH_TOAN", 0, false, true);
        }
        if ("THANH_CONG".equals(r.gatewayResult())) {
            repo.updateOrderStatus("DA_XAC_NHAN");
            repo.recordTransaction(r.action(), "DA_THANH_TOAN");
            return new ThanhToanResponse(true, "DA_XAC_NHAN", "DA_XAC_NHAN", "DA_THANH_TOAN", 0, false, false);
        }
        return new ThanhToanResponse(true, "CHO_THANH_TOAN", r.orderStatus(), "CHUA_THANH_TOAN", 0, false, false);
    }

    private ThanhToanResponse handleCod(ThanhToanRequest r) {
        repo.recordTransaction(r.action(), "DA_THANH_TOAN");
        return new ThanhToanResponse(true, "DA_GIAO", r.orderStatus(), "DA_THANH_TOAN", 0, false, false);
    }

    private ThanhToanResponse handleTimeout(ThanhToanRequest r) {
        repo.updateOrderStatus("DA_HUY");
        repo.releaseStock();
        return new ThanhToanResponse(true, "DA_HUY", "DA_HUY", "CHUA_THANH_TOAN", 0, true, false);
    }

    private ThanhToanResponse handleCancel(ThanhToanRequest r) {
        repo.updateOrderStatus("DA_HUY");
        repo.releaseStock();
        double refund = r.recordedBefore() > 0 ? r.totalAmount() : 0;
        String paymentStatus = r.recordedBefore() > 0 ? "DA_HOAN_TIEN" : "CHUA_THANH_TOAN";
        return new ThanhToanResponse(true, "DA_HUY", "DA_HUY", paymentStatus, refund, true, false);
    }
}

