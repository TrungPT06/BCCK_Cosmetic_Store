package starter.entity;

/**
 * Value Object biểu diễn một dòng sản phẩm trong giỏ hàng.
 */
public record CartLine(Sku sku, int quantity) {
    public CartLine {
        if (sku == null) {
            throw new IllegalArgumentException("SKU không được null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
    }

    public static CartLine of(String skuCode, int quantity) {
        return new CartLine(Sku.of(skuCode), quantity);
    }
}
