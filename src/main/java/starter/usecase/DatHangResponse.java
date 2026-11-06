package starter.usecase;

/**
 * DTO phản hồi thuần túy được tiêu thụ bởi presenter/controller.
 *
 * color đã được loại bỏ — đó là mối bận tâm hiển thị thuộc tầng Adapter (DatHangPresenter).
 * UseCase chỉ trả dữ liệu nghiệp vụ, Presenter chịu trách nhiệm ánh xạ sang màu hiển thị.
 */
public record DatHangResponse(boolean success, String message, String orderStatus, double totalAmount,
                              double shippingFee, int remainingStock, boolean stored,
                              String orderFlag, double giftLinePrice, int remainingGiftStock) {
    public static DatHangResponse failure(String error) {
        return new DatHangResponse(false, error, null, 0, 0, 0, false, null, 0, 0);
    }
}
