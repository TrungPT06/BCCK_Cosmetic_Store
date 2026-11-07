package starter.dathang.adapter;

import starter.dathang.usecase.DatHangOutputBoundary;
import starter.dathang.usecase.DatHangResponse;

import java.time.LocalDate;

/**
 * Interface Adapter: DatHangPresenter.
 * Nhận DatHangResponse từ Use Case và chuẩn bị DatHangViewModel cho UI/Client.
 */
public final class DatHangPresenter implements DatHangOutputBoundary {
    private DatHangViewModel viewModel;

    @Override
    public void present(DatHangResponse response) {
        String color = response.success() ? "green" : "red";
        this.viewModel = new DatHangViewModel(
                response.success(),
                response.orderId(),
                response.orderStatus(),
                response.message(),
                response.totalAmount() != null ? response.totalAmount().amount() : 0L,
                response.shippingFee() != null ? response.shippingFee().amount() : 0L,
                response.remainingStock(),
                response.saved(),
                response.orderFlag(),
                response.giftLinePrice() != null ? response.giftLinePrice().amount() : 0L,
                response.giftStockAfter(),
                color,
                response.processedDate()
        );
    }

    public DatHangViewModel getViewModel() {
        return viewModel;
    }

    public record DatHangViewModel(
            boolean success,
            String orderId,
            String orderStatus,
            String message,
            long totalAmount,
            long shippingFee,
            int remainingStock,
            boolean saved,
            String orderFlag,
            long giftLinePrice,
            int giftStockAfter,
            String color,
            LocalDate processedDate
    ) {}
}
