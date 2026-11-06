package starter.entity;

/**
 * Enum biểu diễn các trạng thái chuẩn trong vòng đời đơn hàng.
 */
public enum OrderStatus {
    CHO_THANH_TOAN("CHO_THANH_TOAN"),
    DA_XAC_NHAN("DA_XAC_NHAN"),
    DANG_GIAO("DANG_GIAO"),
    DA_GIAO("DA_GIAO"),
    DA_HUY("DA_HUY");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Mã trạng thái đơn hàng không hợp lệ: " + code);
    }
}
