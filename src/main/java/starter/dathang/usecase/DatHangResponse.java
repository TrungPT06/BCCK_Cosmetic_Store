package starter.dathang.usecase;

import starter.domain.Money;

import java.time.LocalDate;

/**
 * Pure DTO: Kết quả xử lý đặt hàng gửi tới Output Boundary.
 */
public record DatHangResponse(
        boolean success,
        String orderId,
        String orderStatus,
        String message,
        Money totalAmount,
        Money shippingFee,
        int remainingStock,
        boolean saved,
        String orderFlag,
        Money giftLinePrice,
        int giftStockAfter,
        LocalDate processedDate
) {
    public static DatHangResponse failure(String message, LocalDate processedDate) {
        return new DatHangResponse(false, null, null, message, Money.ZERO, Money.ZERO, 0, false, null, Money.ZERO, 0, processedDate);
    }

    public static DatHangResponse success(String orderId, String orderStatus, Money totalAmount, Money shippingFee,
                                          int remainingStock, String orderFlag, Money giftLinePrice, int giftStockAfter,
                                          LocalDate processedDate) {
        return new DatHangResponse(true, orderId, orderStatus, "DAT_HANG_THANH_CONG", totalAmount, shippingFee,
                remainingStock, true, orderFlag, giftLinePrice, giftStockAfter, processedDate);
    }
}
