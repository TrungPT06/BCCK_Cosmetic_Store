package starter.usecase;

/**
 * Output Boundary cho Use Case Thanh Toán (Clean Architecture / B-C-E).
 * Định nghĩa ranh giới chuyển giao kết quả ra tầng ngoài, không phụ thuộc tầng Adapter.
 */
public interface ThanhToanOutputBoundary {
    void present(ThanhToanResponse response);
}
