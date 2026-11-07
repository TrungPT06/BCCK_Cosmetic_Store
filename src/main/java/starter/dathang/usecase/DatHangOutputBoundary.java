package starter.dathang.usecase;

/**
 * Output Boundary: Cổng ra duy nhất cho kết quả của trường hợp sử dụng Đặt hàng.
 */
public interface DatHangOutputBoundary {
    void present(DatHangResponse response);
}
