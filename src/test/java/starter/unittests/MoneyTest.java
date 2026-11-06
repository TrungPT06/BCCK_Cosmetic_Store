package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.Money;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Value Object Money (DDD)")
class MoneyTest {

    @Test
    @DisplayName("Khởi tạo số tiền hợp lệ và đảm bảo bất biến không âm")
    void createsValidMoney() {
        Money m = Money.of(150_000.0);
        assertEquals(150_000.0, m.amount());
        assertEquals("150000.0", m.toString());

        assertEquals(Money.ZERO, Money.of(0.0));
        assertEquals(Money.ZERO, Money.of(-500.0));
    }

    @Test
    @DisplayName("Cộng hai số tiền")
    void addition() {
        Money m1 = Money.of(100_000.0);
        Money m2 = Money.of(50_000.0);
        assertEquals(Money.of(150_000.0), m1.plus(m2));
    }

    @Test
    @DisplayName("Trừ hai số tiền không bao giờ làm âm số tiền")
    void subtractionNeverNegative() {
        Money m1 = Money.of(100_000.0);
        Money m2 = Money.of(150_000.0);
        assertEquals(Money.ZERO, m1.minus(m2));
    }

    @Test
    @DisplayName("So sánh hai lượng tiền")
    void comparisons() {
        Money m1 = Money.of(500_000.0);
        Money m2 = Money.of(300_000.0);
        assertTrue(m1.isGreaterThan(m2));
        assertTrue(m1.isGreaterThanOrEqualTo(Money.of(500_000.0)));
        assertTrue(m2.isLessThan(m1));
    }
}
