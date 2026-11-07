package starter.thanhtoan.usecase;

/**
 * Input Boundary: Cổng vào trường hợp sử dụng Thanh toán.
 * execute() trả về void; kết quả gửi qua ThanhToanOutputBoundary.
 */
public interface ThanhToanInputBoundary {
    void execute(ThanhToanRequest request);
}
