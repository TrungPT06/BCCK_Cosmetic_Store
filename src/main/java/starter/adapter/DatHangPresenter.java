package starter.adapter;

import starter.usecase.DatHangResponse;

/**
 * Humble presenter: maps the response DTO to display-only data.
 *
 * Single Responsibility: chuyển đổi dữ liệu nghiệp vụ sang dữ liệu hiển thị.
 * determineStatusColor thuộc về đây vì màu sắc là concern trình bày (presentation),
 * không phải quy tắc nghiệp vụ doanh nghiệp (Enterprise Business Rules).
 */
public final class DatHangPresenter {
    public DatHangViewModel present(DatHangResponse r) {
        String color = r.success() ? determineStatusColor(r.orderStatus()) : "red";
        return new DatHangViewModel(r.message(), r.totalAmount(), color);
    }

    public String determineStatusColor(String status) {
        if ("DA_XAC_NHAN".equals(status)) return "green";
        if ("CHO_THANH_TOAN".equals(status)) return "yellow";
        return "red";
    }

    public record DatHangViewModel(String message, double totalAmount, String color) { }
}
