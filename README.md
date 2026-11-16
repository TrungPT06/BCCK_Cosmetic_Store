Website Giới Thiệu & Bán Mỹ Phẩm Trực Tuyến (Clean Architecture)

Dự án môn học Thiết kế Xây dựng Phần mềm (TK_XDPM), áp dụng kiến trúc Clean Architecture (Robert C. Martin), Domain-Driven Design (DDD) và phương pháp Behavior-Driven Development (BDD) với Serenity BDD Living Documentation.

Repository: TrungPT06/BCCK_Cosmetic_Store

Vai trò cá nhân (My Contributions)

Đặc tả nghiệp vụ & Kịch bản kiểm thử (BDD / Gherkin): Trực tiếp thiết kế và viết các tệp đặc tả .feature bằng ngôn ngữ Gherkin cho hai luồng nghiệp vụ chính gồm Đặt hàng (dat_hang.feature) (kiểm tra tồn kho, hạn sử dụng, voucher, phí ship, quà tặng kèm) và Thanh toán (thanh_toan.feature) (callback online VNPAY/MoMo, xác nhận COD, hủy đơn & hoàn tiền).

Kiểm chứng mã nguồn & Theo dõi kết quả kiểm thử: Sử dụng công cụ AI hỗ trợ sinh mã nguồn và Step Definitions từ đặc tả; trực tiếp thực thi bộ kiểm thử tự động (mvn clean verify) và quan sát, đối chiếu kết quả đúng/sai trên báo cáo Serenity BDD Living Documentation để xác minh hệ thống hoạt động đúng với đặc tả nghiệp vụ.

Công nghệ sử dụng (Tech Stack)

Ngôn ngữ & Build: Java (OpenJDK 25+), Maven 3.9+

Kiến trúc: Clean Architecture, Domain-Driven Design (DDD), State & Strategy Pattern

Kiểm thử & Tài liệu sống: Cucumber (Gherkin), Serenity BDD, JUnit, ArchUnit

Cơ sở dữ liệu & CI/CD: In-Memory Repository, H2 Database (JDBC), GitHub Actions (auto-deploy GitHub Pages)

1. Giới thiệu đề tài & Phạm vi

Hệ thống tập trung triển khai chuyên sâu 2 tính năng nghiệp vụ cốt lõi theo mô hình Clean Architecture và DDD:

Đặt hàng (DatHang):

Kiểm tra vai trò, trạng thái xác thực tài khoản của khách hàng.

Thẩm định giỏ hàng (tồn kho khả dụng, hạn sử dụng sản phẩm > 6 tháng, sản phẩm còn hạn).

Xác thực thông tin nhận hàng (địa chỉ, số điện thoại chuẩn Việt Nam).

Áp dụng voucher giảm giá (hạn dùng, lượt dùng, giá trị đơn tối thiểu).

Tính toán phí giao hàng theo chính sách và phân loại đơn hàng (hàng hạn chế vận chuyển như nước hoa).

Tự động tặng kèm mẫu thử (quà tặng kèm) theo chính sách khuyến mãi.

Giữ chỗ tồn kho (reserve stock) và giảm lượt dùng voucher.

Thanh toán (ThanhToan) (tách biệt rõ ràng theo nguyên tắc Đơn nhiệm - SRP):

Thanh toán trực tuyến (Online Payment): Xác thực chữ ký số HMAC, số tiền callback từ cổng thanh toán (VNPAY, MoMo), xử lý idempotency (chống trừ tiền / callback lặp).

Xác nhận thu tiền COD: Dành cho vai trò quản trị viên khi đơn hàng đã giao thành công.

Hủy đơn hàng: Hủy thủ công (hoàn tiền nếu đã thanh toán, nhả tồn kho) và tự động hủy đơn quá hạn thanh toán 30 phút.

2. Cấu trúc Package theo Lát cắt dọc (Vertical Slice)

Hệ thống được tổ chức theo trục nghiệp vụ dọc kết hợp các tầng Clean Architecture dưới src/main/java/starter/:

domain/:

Entities & Value Objects (Money.java, Sku.java, Product.java, Voucher.java, ShippingInfo.java, Order.java, OrderFactory.java, OrderStatus.java)

state/: OrderState.java, PendingPaymentState.java, ConfirmedState.java, DeliveringState.java, DeliveredState.java, CancelledState.java, OrderStateFactory.java

strategy/: PaymentStrategy.java, CodPaymentStrategy.java, OnlinePaymentStrategy.java, PaymentStrategyFactory.java, ShippingFeeStrategy.java, StandardShippingFeeStrategy.java

shared/port/: OrderRepository.java, InventoryRepository.java

dathang/:

usecase/: DatHangInputBoundary.java, DatHangOutputBoundary.java, DatHangRequest.java, DatHangResponse.java, DatHangUseCase.java, ProductRepository.java, VoucherRepository.java

adapter/: DatHangPresenter.java, DatHangController.java

thanhtoan/:

usecase/: ThanhToanInputBoundary.java, ThanhToanOutputBoundary.java, ThanhToanRequest.java, ThanhToanResponse.java, ThanhToanOnlineUseCase.java, XacNhanThuTienCodUseCase.java, HuyDonUseCase.java, PaymentGateway.java, PaymentRepository.java

adapter/: ThanhToanPresenter.java, ThanhToanController.java

infrastructure/: InMemoryOrderRepository.java, InMemoryProductRepository.java, InMemoryVoucherRepository.java, InMemoryInventoryRepository.java, InMemoryPaymentRepository.java, JdbcOrderRepository.java (Adapter cơ sở dữ liệu quan hệ H2 - Tracer Bullet)

app/: AppContainer.java (Composition Root - nơi khởi tạo & đấu nối dependency IoC)

3. Quy tắc Kiến trúc & Bằng chứng

Quy tắc phụ thuộc (Dependency Rule): Các tầng bên trong không bao giờ phụ thuộc tầng bên ngoài. Được kiểm tra tự động bằng ArchUnit test (ArchitectureTest.java).

Độc lập Framework & Database: Sử dụng các Repository Port (ISP). Hỗ trợ song song cả cơ chế In-Memory lẫn JDBC H2 ("Leave open options").

Boundary là lối ra duy nhất: Các Use Case trả về void, kết quả được truyền qua OutputBoundary tới Presenter.

Tracer Bullet: Cung cấp một lát cắt xuyên suốt từ Controller HTTP/API, Use Case, Domain cho tới Database H2 thực tế, có bộ test hợp đồng xác minh tính thay thế lẫn nhau.

4. Ảnh chụp màn hình (Screenshots)

Chụp 1–2 ảnh báo cáo Serenity BDD (target/site/serenity/index.html) lưu vào docs/screenshots/ rồi điền vào bảng dưới đây (hoặc tạm xóa mục này nếu chưa kịp chụp ảnh).

Tổng quan báo cáo Serenity BDD

Chi tiết kịch bản kiểm thử (.feature)





5. Hướng dẫn chạy và sinh Báo cáo Serenity (Living Documentation)

Yêu cầu môi trường:

Java: OpenJDK 25 trở lên.

Maven: 3.9 trở lên.

Lệnh thực thi toàn bộ kiểm thử và báo cáo (chạy qua Maven Failsafe):

mvn clean verify


Xem kết quả Living Documentation:

Interactive Report: target/site/serenity/index.html

Single Page Summary: target/site/serenity/serenity-summary.html

CI/CD: Khi push lên nhánh main, quy trình GitHub Actions (.github/workflows/living-documentation.yml) sẽ tự động chạy mvn clean verify và deploy báo cáo lên GitHub Pages (nhánh gh-pages).
