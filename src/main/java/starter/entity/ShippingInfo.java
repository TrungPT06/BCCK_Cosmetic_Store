package starter.entity;

/**
 * Value Object biểu diễn thông tin giao nhận hàng.
 * Đóng gói quy tắc kiểm tra tính hợp lệ của địa chỉ và số điện thoại nhận hàng.
 */
public record ShippingInfo(String recipientName, String phone, String address, String city) {

    public static ShippingInfo of(String recipientName, String phone, String address, String city) {
        return new ShippingInfo(recipientName, phone, address, city);
    }

    public static ShippingInfo validDefault() {
        return new ShippingInfo("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
    }

    public static ShippingInfo missingAddress() {
        return new ShippingInfo("Nguyễn Văn An", "0912345678", "", "TP. Hồ Chí Minh");
    }

    public static ShippingInfo invalidPhone() {
        return new ShippingInfo("Nguyễn Văn An", "12345", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
    }

    public static ShippingInfo from(String text) {
        if (text == null || "HOP_LE".equalsIgnoreCase(text)) {
            return validDefault();
        }
        if ("THIEU_DIA_CHI".equalsIgnoreCase(text)) {
            return missingAddress();
        }
        if ("SDT_KHONG_HOP_LE".equalsIgnoreCase(text)) {
            return invalidPhone();
        }
        return validDefault();
    }

    /**
     * Xác thực tính hợp lệ của thông tin giao hàng.
     * @return null nếu hợp lệ, mã lỗi nếu vi phạm bất biến
     */
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
