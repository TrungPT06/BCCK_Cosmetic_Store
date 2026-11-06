package starter.adapter;

import starter.usecase.ThanhToanResponse;

/** Presenter phục vụ xử lý và hiển thị thông tin thanh toán. */
public final class ThanhToanPresenter {
    public ThanhToanViewModel present(ThanhToanResponse r) {
        String color = r.success() ? determineColor(r) : "red";
        return new ThanhToanViewModel(r.orderStatusAfter(), r.paymentStatus(), color);
    }

    private String determineColor(ThanhToanResponse r) {
        if (r.isDuplicate()) return "yellow";
        if ("DA_HUY".equals(r.orderStatusAfter())) return "yellow";
        if ("CHUA_THANH_TOAN".equals(r.paymentStatus())) return "yellow";
        if ("DA_HOAN_TIEN".equals(r.paymentStatus())) return "yellow";
        return "green";
    }

    public record ThanhToanViewModel(String orderStatus, String paymentStatus, String color) {}
}

