package starter.thanhtoan.adapter;

import starter.thanhtoan.usecase.ThanhToanOutputBoundary;
import starter.thanhtoan.usecase.ThanhToanResponse;

import java.time.LocalDate;

/**
 * Interface Adapter: ThanhToanPresenter.
 * Chuẩn bị ViewModel cho kết quả thanh toán.
 */
public final class ThanhToanPresenter implements ThanhToanOutputBoundary {
    private ThanhToanViewModel viewModel;

    @Override
    public void present(ThanhToanResponse response) {
        String color = response.success() ? "green" : "red";
        this.viewModel = new ThanhToanViewModel(
                response.success(),
                response.orderId(),
                response.orderStatusBefore(),
                response.orderStatusAfter(),
                response.paymentStatus(),
                response.message(),
                response.refundAmount() != null ? response.refundAmount().amount() : 0L,
                response.stockReleased(),
                response.duplicateCallback(),
                color,
                response.processedDate()
        );
    }

    public ThanhToanViewModel getViewModel() {
        return viewModel;
    }

    public record ThanhToanViewModel(
            boolean success,
            String orderId,
            String orderStatusBefore,
            String orderStatusAfter,
            String paymentStatus,
            String message,
            long refundAmount,
            boolean stockReleased,
            boolean duplicateCallback,
            String color,
            LocalDate processedDate
    ) {}
}
