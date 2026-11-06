package starter.entity;

import java.util.Objects;

/**
 * Value Object biểu diễn mã định danh sản phẩm (Stock Keeping Unit).
 */
public final class Sku {
    public static final Sku NONE = new Sku("KHONG_CO");

    private final String value;

    public Sku(String value) {
        if (value == null || value.trim().isEmpty()) {
            this.value = "KHONG_CO";
        } else {
            this.value = value.trim();
        }
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    public String value() {
        return value;
    }

    public boolean isNone() {
        return "KHONG_CO".equalsIgnoreCase(value);
    }

    public boolean isRestricted() {
        return value.startsWith("NUOC_HOA_");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sku sku)) return false;
        return Objects.equals(value, sku.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
