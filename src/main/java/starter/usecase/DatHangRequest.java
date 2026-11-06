package starter.usecase;

import java.time.LocalDate;

/**
 * DTO yêu cầu thuần túy: không chứa kiểu dữ liệu HTTP, ORM hay UI vượt qua ranh giới ứng dụng.
 *
 * availableStock và giftStock đã được loại bỏ — UseCase tự truy vấn Repository
 * để lấy tồn kho, đảm bảo nguồn dữ liệu chân lý duy nhất (Single Source of Truth)
 * và không rò rỉ mối bận tâm hệ thống vào dữ liệu đầu vào của người dùng.
 */
public record DatHangRequest(String role, boolean verified, String sku, int quantity, double unitPrice,
                             LocalDate expiryDate, String shippingInfo, String voucher,
                             double discount, String paymentMethod, LocalDate today,
                             String giftSku) { }
