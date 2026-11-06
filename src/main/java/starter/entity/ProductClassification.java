package starter.entity;

/**
 * Quy tắc doanh nghiệp (Enterprise Rule) phân loại sản phẩm theo ràng buộc vận chuyển / quy định.
 *
 * Nguyên tắc đơn nhiệm (Single Responsibility): chỉ chịu trách nhiệm phân loại sản phẩm theo cờ vận chuyển.
 * Thay đổi khi: danh mục sản phẩm hạn chế vận chuyển thay đổi (ví dụ thêm hóa chất, pin...).
 */
public final class ProductClassification {
    public String determineFlag(String sku) {
        return (sku != null && sku.startsWith("NUOC_HOA_")) ? "HANG_HAN_CHE" : "KHONG_CO";
    }
}

