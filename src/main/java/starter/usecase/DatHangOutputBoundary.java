package starter.usecase;

/**
 * Output Boundary (Cổng đầu ra theo Clean Architecture / B-C-E).
 * Nằm hoàn toàn ở tầng Use Case.
 * Use Case chỉ phụ thuộc vào interface này để gửi kết quả ra ngoài (DIP),
 * tuyệt đối không phụ thuộc vào Presenter hay ViewModel của tầng Adapter (The Dependency Rule).
 */
public interface DatHangOutputBoundary {
    void present(DatHangResponse response);
}
