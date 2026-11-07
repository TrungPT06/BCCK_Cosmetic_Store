package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Value Object Money (long VNĐ, ném ngoại lệ khi âm)")
class MoneyTest {

    @Test
    @DisplayName("Khởi tạo số tiền hợp lệ và đảm bảo bất biến không âm")
    void createsValidMoney() {
        Money m = Money.of(150_000L);
        assertEquals(150_000L, m.amount());
        assertEquals("150000", m.toString());

        assertEquals(Money.ZERO, Money.of(0L));
    }

    @Test
    @DisplayName("Money.of(số âm) phải ném ngoại lệ IllegalArgumentException")
    void negativeAmountThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Money.of(-500L));
        assertTrue(ex.getMessage().contains("không được âm"));
    }

    @Test
    @DisplayName("Cộng hai số tiền")
    void addition() {
        Money m1 = Money.of(100_000L);
        Money m2 = Money.of(50_000L);
        assertEquals(Money.of(150_000L), m1.plus(m2));
    }

    @Test
    @DisplayName("Trừ hai số tiền không bao giờ làm âm số tiền")
    void subtractionClampsToZero() {
        Money m1 = Money.of(100_000L);
        Money m2 = Money.of(150_000L);
        assertEquals(Money.ZERO, m1.minus(m2));
    }

    @Test
    @DisplayName("So sánh hai lượng tiền")
    void comparisons() {
        Money m1 = Money.of(500_000L);
        Money m2 = Money.of(300_000L);
        assertTrue(m1.isGreaterThan(m2));
        assertTrue(m1.isGreaterThanOrEqualTo(Money.of(500_000L)));
        assertTrue(m2.isLessThan(m1));
    }
}
