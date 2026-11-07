package starter.domain;

import java.util.Objects;

/**
 * Value Object biểu diễn tiền tệ trong hệ thống (Đơn vị: long VNĐ).
 * Đảm bảo tính bất biến (immutability), không dùng số thực, cấm số âm.
 */
public final class Money implements Comparable<Money> {
    public static final Money ZERO = new Money(0L);

    private final long amount;

    private Money(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền không được âm: " + amount);
        }
        this.amount = amount;
    }

    public static Money of(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền không được âm: " + amount);
        }
        if (amount == 0L) {
            return ZERO;
        }
        return new Money(amount);
    }

    public long amount() {
        return amount;
    }

    public Money plus(Money other) {
        return Money.of(this.amount + other.amount);
    }

    public Money minus(Money other) {
        long result = this.amount - other.amount;
        return result <= 0L ? ZERO : Money.of(result);
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
        return Long.compare(this.amount, o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount == money.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.valueOf(amount);
    }
}
