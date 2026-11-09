# Website Giới Thiệu & Bán Mỹ Phẩm Trực Tuyến (Clean Architecture)

Dự án môn học: **Thiết kế Xây dựng Phần mềm (TK_XDPM)**  
Kiến trúc: **Clean Architecture (Robert C. Martin) & Domain-Driven Design (DDD)**  
Tài liệu sống: **Serenity BDD Living Documentation with Cucumber**

---

## 1. Giới thiệu đề tài & Phạm vi

Hệ thống tập trung triển khai chuyên sâu 2 tính năng nghiệp vụ cốt lõi theo mô hình Clean Architecture và DDD:
1. **Đặt hàng (DatHang)**:
   - Kiểm tra vai trò, trạng thái xác thực tài khoản của khách hàng.
   - Thẩm định giỏ hàng (tồn kho khả dụng, hạn sử dụng sản phẩm > 6 tháng, sản phẩm còn hạn).
   - Xác thực thông tin nhận hàng (địa chỉ, số điện thoại chuẩn Việt Nam).
   - Áp dụng voucher giảm giá (hạn dùng, lượt dùng, giá trị đơn tối thiểu).
   - Tính toán phí giao hàng theo chính sách và phân loại đơn hàng (hàng hạn chế vận chuyển như nước hoa).
   - Tự động tặng kèm mẫu thử (quà tặng kèm) theo chính sách khuyến mãi.
   - Giữ chỗ tồn kho (reserve stock) và giảm lượt dùng voucher.
2. **Thanh toán (ThanhToan)**:
   - Tách biệt rõ ràng theo nguyên tắc Đơn nhiệm (SRP):
     - **Thanh toán trực tuyến (Online Payment)**: Xác thực chữ ký số HMAC, số tiền callback từ cổng thanh toán (VNPAY, MoMo), xử lý idempotency (chống trừ tiền / callback lặp).
     - **Xác nhận thu tiền COD**: Dành cho vai trò quản trị viên khi đơn hàng đã giao thành công.
     - **Hủy đơn hàng**: Hủy thủ công (hoàn tiền nếu đã thanh toán, nhả tồn kho) và tự động hủy đơn quá hạn thanh toán 30 phút.

---

## 2. Cấu trúc Package theo Lát cắt dọc (Vertical Slice)

Hệ thống được tổ chức theo trục nghiệp vụ dọc kết hợp các tầng Clean Architecture:

```
src/main/java/starter/
├── domain/                         # Doanh nghiệp cốt lõi (Entities, Value Objects, Domain Policies)
│   ├── Money.java                  # Value Object tiền tệ (long VNĐ, bất biến, cấm số âm)
│   ├── Sku.java                    # Mã định danh sản phẩm
│   ├── Product.java                # Thực thể sản phẩm (giá, hạn dùng, cờ hạn chế)
│   ├── Voucher.java                # Thực thể voucher (mã, mức giảm, hạn dùng, lượt dùng)
│   ├── ShippingInfo.java           # Value Object thông tin giao hàng
│   ├── Order.java                  # Aggregate Root Đơn hàng
│   ├── OrderFactory.java           # Factory tạo đơn hàng bảo vệ bất biến
│   ├── OrderStatus.java            # Enum trạng thái đơn hàng
│   ├── state/                      # State Pattern cho vòng đời Đơn hàng
│   │   ├── OrderState.java
│   │   ├── PendingPaymentState.java
│   │   ├── ConfirmedState.java
│   │   ├── DeliveringState.java
│   │   ├── DeliveredState.java
│   │   ├── CancelledState.java
│   │   └── OrderStateFactory.java
│   └── strategy/                   # Strategy Pattern cho thanh toán & phí ship
│       ├── PaymentStrategy.java
│       ├── CodPaymentStrategy.java
│       ├── OnlinePaymentStrategy.java
│       ├── PaymentStrategyFactory.java
│       ├── ShippingFeeStrategy.java
│       └── StandardShippingFeeStrategy.java
│
├── shared/                         # Dùng chung giữa các lát cắt (gỡ phụ thuộc chéo)
│   └── port/                       # Tầng Use Case: Các Port dùng chung (ISP)
│       ├── OrderRepository.java    # Port lưu trữ đơn hàng (ISP)
│       └── InventoryRepository.java# Port quản lý tồn kho (ISP)
│
├── dathang/                        # Lát cắt dọc: Đặt hàng
│   ├── usecase/                    # Tầng Application Use Case
│   │   ├── DatHangInputBoundary.java
│   │   ├── DatHangOutputBoundary.java
│   │   ├── DatHangRequest.java     # Pure DTO (không chứa logic / entity)
│   │   ├── DatHangResponse.java    # Pure DTO
│   │   ├── DatHangUseCase.java     # Điều phối luồng đặt hàng
│   │   ├── ProductRepository.java  # Port truy vấn sản phẩm
│   │   └── VoucherRepository.java  # Port truy vấn & cập nhật voucher
│   └── adapter/                    # Tầng Interface Adapters
│       ├── DatHangPresenter.java   # Presenter chuẩn bị ViewModel cho UI
│       └── DatHangController.java  # Controller tiếp nhận yêu cầu (Tracer Bullet)
│
├── thanhtoan/                      # Lát cắt dọc: Thanh toán
│   ├── usecase/                    # Tầng Application Use Case (tách theo SRP)
│   │   ├── ThanhToanInputBoundary.java
│   │   ├── ThanhToanOutputBoundary.java
│   │   ├── ThanhToanRequest.java   # Pure DTO
│   │   ├── ThanhToanResponse.java  # Pure DTO
│   │   ├── ThanhToanOnlineUseCase.java
│   │   ├── XacNhanThuTienCodUseCase.java
│   │   ├── HuyDonUseCase.java
│   │   ├── PaymentGateway.java     # Port cổng thanh toán
│   │   └── PaymentRepository.java  # Port ghi nhận nhật ký giao dịch
│   └── adapter/                    # Tầng Interface Adapters
│       ├── ThanhToanPresenter.java # Presenter chuẩn bị ViewModel
│       └── ThanhToanController.java# Controller tiếp nhận yêu cầu (Tracer Bullet)
│
├── infrastructure/                 # Tầng Frameworks & Drivers
│   ├── InMemoryOrderRepository.java
│   ├── InMemoryProductRepository.java
│   ├── InMemoryVoucherRepository.java
│   ├── InMemoryInventoryRepository.java
│   ├── InMemoryPaymentRepository.java
│   └── JdbcOrderRepository.java    # Adapter cơ sở dữ liệu quan hệ H2 (Tracer Bullet)
│
└── app/                            # Composition Root
    └── AppContainer.java           # Nơi khởi tạo & đấu nối dependency (IoC)
```

---

## 3. Quy tắc Kiến trúc & Bằng chứng

- **Quy tắc phụ thuộc (Dependency Rule)**: Các tầng bên trong không bao giờ phụ thuộc tầng bên ngoài. Được kiểm tra tự động bằng **ArchUnit** test (`ArchitectureTest.java`).
- **Độc lập Framework & Database**: Sử dụng các Repository Port (ISP). Hỗ trợ song song cả cơ chế In-Memory lẫn JDBC H2 ("Leave open options").
- **Boundary là lối ra duy nhất**: Các Use Case trả về `void`, kết quả được truyền qua `OutputBoundary` tới `Presenter`.
- **Tracer Bullet**: Cung cấp một lát cắt xuyên suốt từ Controller HTTP/API, Use Case, Domain cho tới Database H2 thực tế, có bộ test hợp đồng xác minh tính thay thế lẫn nhau.

---

## 4. Hướng dẫn chạy và sinh Báo cáo Serenity (Living Documentation)

### Yêu cầu môi trường
- **Java**: OpenJDK 25 trở lên.
- **Maven**: 3.9 trở lên.

### Lệnh thực thi toàn bộ kiểm thử và báo cáo
Dự án cấu hình kiểm thử tích hợp và Gherkin BDD chạy qua Maven Failsafe:

```bash
mvn clean verify
```

### Xem kết quả Living Documentation
Sau khi build thành công, báo cáo Serenity được sinh tự động tại thư mục:
- **Interactive Report**: `target/site/serenity/index.html`
- **Single Page Summary**: `target/site/serenity/serenity-summary.html`

Khi push lên nhánh `main`, quy trình GitHub Actions (`.github/workflows/living-documentation.yml`) sẽ tự động chạy `mvn clean verify` và deploy báo cáo lên **GitHub Pages** (nhánh `gh-pages`).
