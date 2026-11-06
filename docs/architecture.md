# Kiến trúc và thiết kế tính năng

Mở `architecture.drawio` bằng diagrams.net/draw.io. Tệp có năm trang: tổng quan Clean Architecture và một trang UML component cho từng lát cắt dọc.

Mỗi tính năng nhận một Request DTO qua InputBoundary, được use case điều phối bằng policy miền, ghi/đọc qua repository port và trả Response DTO cho presenter. Vì vậy UI, persistence và cổng thanh toán là các chi tiết có thể thay thế mà không tạo phụ thuộc ngược vào nghiệp vụ.

| Tính năng | Đặc tả BDD | Policy miền | Port đầu ra |
|---|---|---|---|
| Đặt hàng | `dat_hang.feature` | `OrderPolicy`, `PricingPolicy`, `ShippingPolicy`, `ProductClassification` | `OrderRepository` |
| Tồn kho | `kiem_tra_ton_kho.feature` | `InventoryPolicy` | `InventoryRepository` |
| Quản lý đơn | `kiem_tra_don_hang.feature` | `OrderLifecyclePolicy` | `OrderManagementRepository` |
| Thanh toán | `thanh_toan.feature` | `PaymentPolicy` | `PaymentRepository` |

Acceptance test (Gherkin + glue) nằm ở vòng ngoài; unit test bảo vệ các policy và use case ở vòng trong. `gradlew.bat test` tạo Living Documentation trong `target/site/serenity/`.
