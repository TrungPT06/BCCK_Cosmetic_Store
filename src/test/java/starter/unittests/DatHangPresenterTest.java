package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.adapter.DatHangPresenter;
import starter.usecase.DatHangResponse;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho DatHangPresenter (Humble Object - Interface Adapter)")
class DatHangPresenterTest {

    private final DatHangPresenter presenter = new DatHangPresenter();

    @Test
    @DisplayName("Đơn COD đã xác nhận hiển thị màu xanh lá (green)")
    void confirmedOrderShowsGreen() {
        var response = new DatHangResponse(true, "DA_XAC_NHAN", "DA_XAC_NHAN",
                900_000, 0, 10, true, "KHONG_CO", 0, 0);
        var vm = presenter.present(response);
        assertEquals("green", vm.color());
        assertEquals("DA_XAC_NHAN", vm.message());
        assertEquals(900_000, vm.totalAmount());
    }

    @Test
    @DisplayName("Đơn chờ thanh toán online hiển thị màu vàng (yellow)")
    void pendingPaymentShowsYellow() {
        var response = new DatHangResponse(true, "CHO_THANH_TOAN", "CHO_THANH_TOAN",
                380_000, 30_000, 14, true, "KHONG_CO", 0, 0);
        var vm = presenter.present(response);
        assertEquals("yellow", vm.color());
    }

    @Test
    @DisplayName("Đơn hàng lỗi luôn hiển thị màu đỏ (red)")
    void failedOrderShowsRed() {
        var response = DatHangResponse.failure("GIO_HANG_TRONG");
        var vm = presenter.present(response);
        assertEquals("red", vm.color());
        assertEquals("GIO_HANG_TRONG", vm.message());
    }

    @Test
    @DisplayName("Trạng thái không xác định hiển thị màu đỏ (red)")
    void unknownStatusShowsRed() {
        assertEquals("red", presenter.determineStatusColor("THAT_BAI"));
        assertEquals("red", presenter.determineStatusColor("UNKNOWN"));
    }
}

