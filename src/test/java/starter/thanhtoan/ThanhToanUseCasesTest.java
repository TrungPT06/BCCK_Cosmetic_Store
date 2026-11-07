package starter.thanhtoan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.domain.*;
import starter.domain.state.DeliveredState;
import starter.infrastructure.FakePaymentGateway;
import starter.infrastructure.InMemoryInventoryRepository;
import starter.infrastructure.InMemoryOrderRepository;
import starter.infrastructure.InMemoryPaymentRepository;
import starter.thanhtoan.adapter.ThanhToanPresenter;
import starter.thanhtoan.usecase.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho các Use Case thanh toán (SRP, Boundary void, Bug 1, Bug 2)")
class ThanhToanUseCasesTest {
    private InMemoryOrderRepository orderRepo;
    private InMemoryInventoryRepository inventoryRepo;
    private InMemoryPaymentRepository paymentRepo;
    private FakePaymentGateway gateway;
    private ThanhToanPresenter presenter;
    private Clock fixedClock;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        orderRepo = new InMemoryOrderRepository();
        inventoryRepo = new InMemoryInventoryRepository();
        paymentRepo = new InMemoryPaymentRepository();
        gateway = new FakePaymentGateway();
        presenter = new ThanhToanPresenter();
        fixedClock = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        now = LocalDateTime.now(fixedClock);

        inventoryRepo.reserve("SERUM_VITC_30", -10); // stock = 10
    }

    private Order createTestOrder(String id, String customerId, LocalDateTime createdAt,
                                  OrderStatus status, Money amount) {
        Order order = new Order(id, customerId, createdAt, status, amount, Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, amount, false)));
        orderRepo.save(order);
        return order;
    }

    @Test
    @DisplayName("ThanhToanOnline: Giao dịch thành công, chữ ký khớp, cập nhật đơn sang DA_XAC_NHAN")
    void onlinePaymentSuccess() {
        createTestOrder("ORD-1", "an", now.minusMinutes(5), OrderStatus.CHO_THANH_TOAN, Money.of(700_000L));
        var useCase = new ThanhToanOnlineUseCase(orderRepo, paymentRepo, gateway, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-1", "an", "customer", "THANH_TOAN_ONLINE",
                "THANH_CONG", Money.of(700_000L), "HOP_LE");

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertTrue(vm.success());
        assertEquals("DA_XAC_NHAN", vm.orderStatusAfter());
        assertEquals("DA_THANH_TOAN", vm.paymentStatus());
        assertEquals(1, paymentRepo.transactionCount("ORD-1"));
    }

    @Test
    @DisplayName("ThanhToanOnline: Chữ ký callback không hợp lệ bị từ chối")
    void invalidSignatureRejected() {
        createTestOrder("ORD-2", "an", now.minusMinutes(5), OrderStatus.CHO_THANH_TOAN, Money.of(700_000L));
        var useCase = new ThanhToanOnlineUseCase(orderRepo, paymentRepo, gateway, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-2", "an", "customer", "THANH_TOAN_ONLINE",
                "THANH_CONG", Money.of(700_000L), "KHONG_HOP_LE");

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertFalse(vm.success());
        assertEquals("CHU_KY_CALLBACK_KHONG_HOP_LE", vm.message());
        assertEquals(0, paymentRepo.transactionCount("ORD-2"));
    }

    @Test
    @DisplayName("XacNhanThuTienCod: Quản trị viên xác nhận đơn COD đã giao thành công")
    void adminConfirmsCodForDeliveredOrder() {
        createTestOrder("ORD-3", "an", now.minusDays(3), OrderStatus.DA_GIAO, Money.of(700_000L));
        var useCase = new XacNhanThuTienCodUseCase(orderRepo, paymentRepo, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-3", "admin", "admin", "XAC_NHAN_THU_TIEN_COD",
                null, Money.ZERO, null);

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertTrue(vm.success());
        assertEquals("DA_GIAO", vm.orderStatusAfter());
        assertEquals("DA_THANH_TOAN", vm.paymentStatus());
        assertEquals(1, paymentRepo.transactionCount("ORD-3"));
    }

    @Test
    @DisplayName("Bug 1: Hủy quá hạn khi chưa đủ 30 phút trả lỗi CHUA_QUA_HAN_THANH_TOAN")
    void timeoutCancelBefore30MinutesFails() {
        // Đơn mới tạo cách đây 10 phút (< 30 phút)
        createTestOrder("ORD-4", "an", now.minusMinutes(10), OrderStatus.CHO_THANH_TOAN, Money.of(700_000L));
        var useCase = new HuyDonUseCase(orderRepo, inventoryRepo, paymentRepo, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-4", "he_thong", "he_thong", "HUY_QUA_HAN_THANH_TOAN",
                null, Money.ZERO, null);

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertFalse(vm.success());
        assertEquals("CHUA_QUA_HAN_THANH_TOAN", vm.message());
        assertEquals(OrderStatus.CHO_THANH_TOAN, orderRepo.findById("ORD-4").get().orderStatus());
    }

    @Test
    @DisplayName("Bug 1 (thành công): Hủy quá hạn khi đã qua 31 phút thành công, nhả tồn kho")
    void timeoutCancelAfter31MinutesSuccess() {
        createTestOrder("ORD-5", "an", now.minusMinutes(31), OrderStatus.CHO_THANH_TOAN, Money.of(700_000L));
        var useCase = new HuyDonUseCase(orderRepo, inventoryRepo, paymentRepo, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-5", "he_thong", "he_thong", "HUY_QUA_HAN_THANH_TOAN",
                null, Money.ZERO, null);

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertTrue(vm.success());
        assertEquals("DA_HUY", vm.orderStatusAfter());
        assertTrue(vm.stockReleased());
        assertEquals(11, inventoryRepo.stockOf("SERUM_VITC_30")); // stock restored from 10 to 11
    }

    @Test
    @DisplayName("Bug 2: Không thể hủy đơn hàng đã giao (DA_GIAO), trả lỗi KHONG_THE_HUY_DON_DA_GIAO")
    void cannotCancelDeliveredOrder() {
        Order deliveredOrder = new Order("ORD-6", "an", now.minusDays(1), new DeliveredState(),
                Money.of(700_000L), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(700_000L), false)));
        orderRepo.save(deliveredOrder);

        var useCase = new HuyDonUseCase(orderRepo, inventoryRepo, paymentRepo, presenter, fixedClock);

        var request = new ThanhToanRequest("ORD-6", "an", "customer", "HUY_DON",
                null, Money.ZERO, null);

        useCase.execute(request);

        var vm = presenter.getViewModel();
        assertFalse(vm.success());
        assertEquals("KHONG_THE_HUY_DON_DA_GIAO", vm.message());
        assertEquals(OrderStatus.DA_GIAO, orderRepo.findById("ORD-6").get().orderStatus());
    }
}
