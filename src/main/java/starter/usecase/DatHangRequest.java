package starter.usecase;

import java.time.LocalDate;

/**
 * Plain request DTO: no HTTP, ORM, or UI types cross the application boundary.
 *
 * availableStock và giftStock đã được loại bỏ — UseCase tự query Repository
 * để lấy tồn kho, đảm bảo nguồn dữ liệu duy nhất (Single Source of Truth)
 * và không rò rỉ concern hệ thống vào input của người dùng.
 */
public record DatHangRequest(String role, boolean verified, String sku, int quantity, double unitPrice,
                             LocalDate expiryDate, String shippingInfo, String voucher,
                             double discount, String paymentMethod, LocalDate today,
                             String giftSku) { }
