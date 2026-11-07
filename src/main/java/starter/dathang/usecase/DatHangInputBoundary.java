package starter.dathang.usecase;

/**
 * Input Boundary: Cổng vào trường hợp sử dụng Đặt hàng.
 * execute() trả về void; mọi kết quả được đẩy qua DatHangOutputBoundary.
 */
public interface DatHangInputBoundary {
    void execute(DatHangRequest request);
}
