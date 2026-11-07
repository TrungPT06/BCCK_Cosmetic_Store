package starter.domain;

/**
 * Trạng thái của đơn hàng trong vòng đời nghiệp vụ.
 */
public enum OrderStatus {
    CHO_THANH_TOAN("CHO_THANH_TOAN", "Chờ thanh toán"),
    DA_XAC_NHAN("DA_XAC_NHAN", "Đã xác nhận"),
    DANG_GIAO("DANG_GIAO", "Đang giao"),
    DA_GIAO("DA_GIAO", "Đã giao"),
    DA_HUY("DA_HUY", "Đã hủy");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}
