package starter.domain;

import java.util.Objects;

/**
 * Value Object biểu diễn mã định danh sản phẩm (Stock Keeping Unit).
 */
public record Sku(String value) {
    public Sku {
        Objects.requireNonNull(value, "SKU không được null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU không được để trống");
        }
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
