package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Thực thể Product (hàng hạn chế lưu ở Product)")
class ProductTest {

    @Test
    @DisplayName("Sản phẩm hạn chế (nước hoa) có restricted = true")
    void restrictedProduct() {
        Product perfume = new Product(Sku.of("NUOC_HOA_ROSE_50"), "Nước hoa Rose 50ml",
                Money.of(1_200_000L), LocalDate.of(2028, 1, 1), true);

        assertTrue(perfume.isRestricted());
    }

    @Test
    @DisplayName("Kiểm tra sản phẩm hết hạn hoặc cận hạn dưới 6 tháng")
    void expiryChecks() {
        LocalDate today = LocalDate.of(2026, 11, 4);

        Product expired = new Product(Sku.of("TAY_TRANG_BIO"), "Nước tẩy trang",
                Money.of(400_000L), LocalDate.of(2026, 10, 1), false);
        assertTrue(expired.isExpired(today));

        Product nearExpiry = new Product(Sku.of("MAT_NA_LANEIGE"), "Mặt nạ ngủ",
                Money.of(600_000L), LocalDate.of(2027, 1, 15), false);
        assertFalse(nearExpiry.isExpired(today));
        assertTrue(nearExpiry.isNearExpiry(today));

        Product valid = new Product(Sku.of("SERUM_VITC_30"), "Serum Vitamin C",
                Money.of(450_000L), LocalDate.of(2027, 12, 1), false);
        assertFalse(valid.isExpired(today));
        assertFalse(valid.isNearExpiry(today));
    }
}
