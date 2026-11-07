package starter.thanhtoan.usecase;

/**
 * Output Boundary: Cổng ra duy nhất cho kết quả thanh toán.
 */
public interface ThanhToanOutputBoundary {
    void present(ThanhToanResponse response);
}
