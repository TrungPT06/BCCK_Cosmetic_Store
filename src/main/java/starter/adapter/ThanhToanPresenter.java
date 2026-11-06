package starter.adapter;

import starter.usecase.ThanhToanOutputBoundary;
import starter.usecase.ThanhToanResponse;

/**
 * Presenter phục vụ xử lý và hiển thị thông tin thanh toán (Humble Object).
 * Cài đặt ThanhToanOutputBoundary để nhận kết quả từ ThanhToanUseCase.
 */
public final class ThanhToanPresenter implements ThanhToanOutputBoundary {
    private ThanhToanViewModel viewModel;

    @Override
    public void present(ThanhToanResponse r) {
        String color = r.success() ? determineColor(r) : "red";
        this.viewModel = new ThanhToanViewModel(r.orderStatusAfter(), r.paymentStatus(), color);
    }

    public ThanhToanViewModel getViewModel() {
        return viewModel;
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
