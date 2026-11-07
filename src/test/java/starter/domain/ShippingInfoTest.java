package starter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Value Object ShippingInfo thuần")
class ShippingInfoTest {

    @Test
    @DisplayName("Thông tin giao hàng đầy đủ và hợp lệ")
    void validShippingInfo() {
        ShippingInfo info = new ShippingInfo("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
        assertNull(info.validate());
    }

    @Test
    @DisplayName("Thiếu địa chỉ trả về lỗi THIEU_THONG_TIN_GIAO_HANG")
    void missingAddress() {
        ShippingInfo info = new ShippingInfo("Nguyễn Văn An", "0912345678", "", "TP. Hồ Chí Minh");
        assertEquals("THIEU_THONG_TIN_GIAO_HANG", info.validate());
    }

    @Test
    @DisplayName("Số điện thoại không hợp lệ trả về lỗi SO_DIEN_THOAI_KHONG_HOP_LE")
    void invalidPhone() {
        ShippingInfo info = new ShippingInfo("Nguyễn Văn An", "12345", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
        assertEquals("SO_DIEN_THOAI_KHONG_HOP_LE", info.validate());
    }
}
