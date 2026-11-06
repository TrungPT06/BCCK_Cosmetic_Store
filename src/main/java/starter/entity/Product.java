package starter.entity;

import java.time.LocalDate;

/**
 * Thực thể sản phẩm mỹ phẩm (Product Entity).
 * Đóng gói dữ liệu giá, hạn sử dụng và quy tắc phân loại an toàn hàng hóa.
 */
public class Product {
    private final Sku sku;
    private final String name;
    private final Money unitPrice;
    private final LocalDate expiryDate;
    private final boolean isGift;

    public Product(Sku sku, String name, Money unitPrice, LocalDate expiryDate, boolean isGift) {
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.isGift = isGift;
    }

    public static Product regular(String skuCode, String name, double price, LocalDate expiry) {
        return new Product(Sku.of(skuCode), name, Money.of(price), expiry, false);
    }

    public static Product gift(String skuCode, String name) {
        return new Product(Sku.of(skuCode), name, Money.ZERO, LocalDate.MAX, true);
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

    public boolean isGift() {
        return isGift;
    }

    public boolean isExpired(LocalDate today) {
        return expiryDate != null && !expiryDate.isAfter(today);
    }

    public boolean isNearExpiry(LocalDate today, long monthsThreshold) {
        return expiryDate != null && expiryDate.isBefore(today.plusMonths(monthsThreshold));
    }

    public String determineOrderFlag() {
        return sku.isRestricted() ? "HANG_HAN_CHE" : "KHONG_CO";
    }
}
