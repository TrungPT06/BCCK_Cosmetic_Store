package starter.thanhtoan.usecase;

import starter.domain.Money;

/**
 * Pure DTO: Yêu cầu xử lý thanh toán.
 * Chỉ giữ orderId, userId, role, action và dữ liệu callback; use case tự nạp trạng thái từ OrderRepository.
 */
public record ThanhToanRequest(
        String orderId,
        String userId,
        String role,
        String action,
        String gatewayResult,
        Money gatewayAmount,
        String signature
) {}
