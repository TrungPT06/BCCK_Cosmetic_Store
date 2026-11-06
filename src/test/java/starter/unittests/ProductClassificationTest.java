package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.ProductClassification;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho ProductClassification (Enterprise Business Rules)")
class ProductClassificationTest {

    private final ProductClassification classification = new ProductClassification();

    @Test
    @DisplayName("Sản phẩm nước hoa được gán cờ hàng hạn chế vận chuyển")
    void flagsPerfumeAsRestricted() {
        assertEquals("HANG_HAN_CHE", classification.determineFlag("NUOC_HOA_ROSE_50"));
        assertEquals("HANG_HAN_CHE", classification.determineFlag("NUOC_HOA_CHANEL_100"));
    }

    @Test
    @DisplayName("Sản phẩm thường không gán cờ hạn chế")
    void noFlagForRegularProducts() {
        assertEquals("KHONG_CO", classification.determineFlag("SERUM_VITC_30"));
        assertEquals("KHONG_CO", classification.determineFlag("SON_3CE_TAUPE"));
    }

    @Test
    @DisplayName("SKU null không gán cờ hạn chế")
    void noFlagForNullSku() {
        assertEquals("KHONG_CO", classification.determineFlag(null));
    }
}

