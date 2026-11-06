package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.ShippingInfo;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho Value Object ShippingInfo (DDD)")
class ShippingInfoTest {

    @Test
    @DisplayName("Địa chỉ và số điện thoại hợp lệ vượt qua kiểm tra")
    void validShippingInfo() {
        var info = ShippingInfo.validDefault();
        assertNull(info.validate());
    }

    @Test
    @DisplayName("Thiếu địa chỉ báo lỗi THIEU_THONG_TIN_GIAO_HANG")
    void missingAddress() {
        var info = ShippingInfo.missingAddress();
        assertEquals("THIEU_THONG_TIN_GIAO_HANG", info.validate());
    }

    @Test
    @DisplayName("Số điện thoại không đúng 10 chữ số báo lỗi SO_DIEN_THOAI_KHONG_HOP_LE")
    void invalidPhone() {
        var info = ShippingInfo.invalidPhone();
        assertEquals("SO_DIEN_THOAI_KHONG_HOP_LE", info.validate());

        var custom = new ShippingInfo("An", "0123", "Địa chỉ", "TP.HCM");
        assertEquals("SO_DIEN_THOAI_KHONG_HOP_LE", custom.validate());
    }
}
