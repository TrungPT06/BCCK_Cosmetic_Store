package starter.entity;

import java.util.Objects;

/**
 * Value Object biểu diễn tiền tệ trong hệ thống (Đơn vị: VNĐ).
 * Đảm bảo tính bất biến (immutability) và ngăn ngừa lỗi làm tròn số thực.
 */
public final class Money implements Comparable<Money> {
    public static final Money ZERO = new Money(0.0);

    private final double amount;

    private Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền không được âm: " + amount);
        }
        this.amount = Math.round(amount * 100.0) / 100.0;
    }

    public static Money of(double amount) {
        if (amount <= 0.0) {
            return ZERO;
        }
        return new Money(amount);
    }

    public double amount() {
        return amount;
    }

    public Money plus(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money minus(Money other) {
        double result = this.amount - other.amount;
        return result <= 0.0 ? ZERO : new Money(result);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        return this.amount >= other.amount;
    }

    public boolean isLessThan(Money other) {
        return this.amount < other.amount;
    }

    @Override
    public int compareTo(Money o) {
        return Double.compare(this.amount, o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Double.compare(money.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "%.1f", amount);
    }
}
