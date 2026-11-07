package starter.dathang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.dathang.adapter.DatHangPresenter;
import starter.dathang.usecase.*;
import starter.domain.*;
import starter.domain.strategy.PaymentStrategyFactory;
import starter.domain.strategy.StandardShippingFeeStrategy;
import starter.infrastructure.InMemoryInventoryRepository;
import starter.infrastructure.InMemoryOrderRepository;
import starter.infrastructure.InMemoryProductRepository;
import starter.infrastructure.InMemoryVoucherRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho DatHangUseCase (Clean Architecture, Clock, Pure DTO, Boundary void)")
class DatHangUseCaseTest {
    private InMemoryOrderRepository orderRepo;
    private InMemoryProductRepository productRepo;
    private InMemoryVoucherRepository voucherRepo;
    private InMemoryInventoryRepository inventoryRepo;
    private DatHangPresenter presenter;
    private Clock fixedClock;
    private DatHangUseCase useCase;

    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @BeforeEach
    void setUp() {
        orderRepo = new InMemoryOrderRepository();
        productRepo = new InMemoryProductRepository();
        voucherRepo = new InMemoryVoucherRepository();
        inventoryRepo = new InMemoryInventoryRepository();
        presenter = new DatHangPresenter();
        fixedClock = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

        // Seed products
        productRepo.save(new Product(Sku.of("SERUM_VITC_30"), "Serum Vitamin C 30ml",
                Money.of(450_000L), today.plusYears(1), false));
        productRepo.save(new Product(Sku.of("NUOC_HOA_ROSE_50"), "Nước hoa Rose 50ml",
                Money.of(1_200_000L), today.plusYears(2), true));
        productRepo.save(new Product(Sku.of("MAU_THU_SERUM_5ML"), "Mẫu thử Serum 5ml",
                Money.ZERO, today.plusYears(1), false));

        inventoryRepo.reserve("SERUM_VITC_30", -12); // stock = 12
        inventoryRepo.reserve("NUOC_HOA_ROSE_50", -5); // stock = 5
        inventoryRepo.reserve("MAU_THU_SERUM_5ML", -20); // gift stock = 20

        useCase = new DatHangUseCase(orderRepo, productRepo, voucherRepo, inventoryRepo,
                new OrderPolicy(new PaymentStrategyFactory()),
                new OrderFactory(new PaymentStrategyFactory()),
                new StandardShippingFeeStrategy(),
                presenter, fixedClock);
    }

    @Test
    @DisplayName("Đặt hàng thành công với thông tin hợp lệ (giá và hạn dùng lấy từ repository, execute trả void)")
    void placeOrderSuccess() {
        var request = new DatHangRequest(
                "cust-1", "customer", true,
                List.of(new DatHangRequest.LineItem("SERUM_VITC_30", 2)),
                new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh"),
                "KHONG_DUNG", "COD", "KHONG_CO"
        );

        useCase.execute(request);

        DatHangPresenter.DatHangViewModel vm = presenter.getViewModel();
        assertTrue(vm.success());
        assertEquals("DA_XAC_NHAN", vm.orderStatus());
        assertEquals(900_000L, vm.totalAmount());
        assertEquals(0L, vm.shippingFee());
        assertEquals(10, inventoryRepo.stockOf("SERUM_VITC_30"));
        assertEquals("KHONG_CO", vm.orderFlag());
    }

    @Test
    @DisplayName("Bug 3: Đặt hàng lần 1 trừ lượt dùng của voucher; đặt lần 2 bị từ chối VOUCHER_HET_LUOT_SU_DUNG")
    void voucherUsageDecrementedAndFailsOnSecondOrder() {
        // Voucher chỉ có 1 lượt dùng duy nhất
        voucherRepo.save(new Voucher("GIAM50K", Money.of(50_000L), Money.of(200_000L), today.plusMonths(1), 1));

        var request = new DatHangRequest(
                "cust-1", "customer", true,
                List.of(new DatHangRequest.LineItem("SERUM_VITC_30", 1)),
                new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh"),
                "GIAM50K", "VNPAY", "KHONG_CO"
        );

        // Lần 1: Thành công
        useCase.execute(request);
        assertTrue(presenter.getViewModel().success());
        assertEquals(430_000L, presenter.getViewModel().totalAmount()); // 450k - 50k + 30k ship = 430k

        Voucher savedVoucher = voucherRepo.findByCode("GIAM50K").orElseThrow();
        assertEquals(0, savedVoucher.remainingUses());

        // Lần 2: Thất bại do hết lượt
        useCase.execute(request);
        assertFalse(presenter.getViewModel().success());
        assertEquals("VOUCHER_HET_LUOT_SU_DUNG", presenter.getViewModel().message());
    }

    @Test
    @DisplayName("Đặt hàng với nước hoa tự động gắn cờ HANG_HAN_CHE")
    void placeRestrictedProductOrder() {
        var request = new DatHangRequest(
                "cust-2", "customer", true,
                List.of(new DatHangRequest.LineItem("NUOC_HOA_ROSE_50", 1)),
                new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh"),
                "KHONG_DUNG", "COD", "KHONG_CO"
        );

        useCase.execute(request);

        DatHangPresenter.DatHangViewModel vm = presenter.getViewModel();
        assertTrue(vm.success());
        assertEquals("HANG_HAN_CHE", vm.orderFlag());
    }
}
