package starter.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Thực thể Sản phẩm (Product).
 * Đóng gói thông tin đơn giá, hạn dùng và cờ hạn chế vận chuyển (restricted).
 */
public final class Product {
    private final Sku sku;
    private final String name;
    private final Money unitPrice;
    private final LocalDate expiryDate;
    private final boolean restricted;

    public Product(Sku sku, String name, Money unitPrice, LocalDate expiryDate, boolean restricted) {
        this.sku = Objects.requireNonNull(sku, "SKU không được null");
        this.name = Objects.requireNonNull(name, "Tên sản phẩm không được null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Đơn giá không được null");
        this.expiryDate = Objects.requireNonNull(expiryDate, "Hạn sử dụng không được null");
        this.restricted = restricted;
    }

    public Sku sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public boolean isExpired(LocalDate today) {
        return expiryDate.isBefore(today);
    }

    public boolean isNearExpiry(LocalDate today) {
        return !isExpired(today) && expiryDate.isBefore(today.plusMonths(6));
    }
}
