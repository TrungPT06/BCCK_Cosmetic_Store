package starter.adapter;

import starter.usecase.DatHangOutputBoundary;
import starter.usecase.DatHangResponse;

/**
 * Presenter khiêm tốn (Humble Presenter): cài đặt DatHangOutputBoundary.
 * Chuyển đổi dữ liệu nghiệp vụ (DatHangResponse) sang dữ liệu hiển thị (DatHangViewModel).
 */
public final class DatHangPresenter implements DatHangOutputBoundary {
    private DatHangViewModel viewModel;

    @Override
    public void present(DatHangResponse r) {
        String color = r.success() ? determineStatusColor(r.orderStatus()) : "red";
        this.viewModel = new DatHangViewModel(r.message(), r.totalAmount(), color);
    }

    /** Hỗ trợ tương thích ngược cho các unit test gọi trực tiếp hàm present */
    public DatHangViewModel presentAndGet(DatHangResponse r) {
        present(r);
        return viewModel;
    }

    public DatHangViewModel getViewModel() {
        return viewModel;
    }

    public String determineStatusColor(String status) {
        if ("DA_XAC_NHAN".equals(status)) return "green";
        if ("CHO_THANH_TOAN".equals(status)) return "yellow";
        return "red";
    }

    public record DatHangViewModel(String message, double totalAmount, String color) { }
}
