package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.*;
import starter.infrastructure.InMemoryOrderRepository;
import starter.usecase.DatHangRequest;
import starter.usecase.DatHangUseCase;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho DatHangUseCase (Application Business Rules)")
class DatHangUseCaseTest {
    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @Test void createsCodOrderReservesStockAndWaivesShippingAtThreshold() {
        var repo = new InMemoryOrderRepository(); repo.setStock("SERUM", 12);
        var result = useCase(repo).execute(request("customer", true, "SERUM", 2, 450_000, "COD"));
        assertTrue(result.success()); assertEquals("DA_XAC_NHAN", result.orderStatus());
        assertEquals(900_000, result.totalAmount()); assertEquals(10, result.remainingStock());
        assertTrue(repo.containsOrder());
    }

    @Test void refusesInvalidOrderWithoutChangingInventory() {
        var repo = new InMemoryOrderRepository(); repo.setStock("SERUM", 12);
        var result = useCase(repo).execute(request("guest", false, "SERUM", 1, 450_000, "COD"));
        assertFalse(result.success()); assertEquals("YEU_CAU_DANG_NHAP_HOAC_DANG_KY", result.message());
        assertEquals(12, repo.stockOf("SERUM")); assertFalse(repo.containsOrder());
    }

    @Test void capsVoucherAtMerchandiseValueAndKeepsShipping() {
        var repo = new InMemoryOrderRepository(); repo.setStock("SON", 15);
        var result = useCase(repo).execute(request("customer", true, "SON", 1, 350_000, "VNPAY", "GIAM500K", 500_000));
        assertTrue(result.success()); assertEquals(30_000, result.totalAmount()); assertEquals("CHO_THANH_TOAN", result.orderStatus());
    }

    private DatHangUseCase useCase(InMemoryOrderRepository repo) {
        return new DatHangUseCase(repo, new OrderPolicy(), new ShippingPolicy(), new PricingPolicy(), new ProductClassification());
    }
    private DatHangRequest request(String role, boolean verified, String sku, int qty, double price, String payment) {
        return request(role, verified, sku, qty, price, payment, "KHONG_DUNG", 0);
    }
    private DatHangRequest request(String role, boolean verified, String sku, int qty, double price, String payment, String voucher, double discount) {
        return new DatHangRequest(role, verified, sku, qty, price, today.plusMonths(12), "HOP_LE", voucher, discount, payment, today, "KHONG_CO");
    }
}
