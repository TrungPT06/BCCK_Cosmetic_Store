package starter.domain;

/**
 * Value Object biểu diễn thông tin giao nhận hàng.
 * Đóng gói quy tắc kiểm tra tính hợp lệ của địa chỉ và số điện thoại nhận hàng.
 * Thuần túy nghiệp vụ, không chứa các hàm dựng fixture cho test.
 */
public record ShippingInfo(String recipientName, String phone, String address, String city) {

    public String validate() {
        if (address == null || address.trim().isEmpty()) {
            return "THIEU_THONG_TIN_GIAO_HANG";
        }
        if (phone == null || !isValidPhone(phone)) {
            return "SO_DIEN_THOAI_KHONG_HOP_LE";
        }
        return null;
    }

    private boolean isValidPhone(String p) {
        String trimmed = p.trim();
        return trimmed.matches("^0\\d{9}$");
    }
}
