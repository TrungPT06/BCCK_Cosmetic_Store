package starter.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import starter.shared.port.OrderRepository;
import starter.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử hợp đồng (Contract Test) cho OrderRepository (Leave open options: InMemory vs H2)")
abstract class OrderRepositoryContractTest {

    protected abstract OrderRepository createRepository();

    @Test
    @DisplayName("Lưu và tìm lại đơn hàng chính xác theo ID")
    void saveAndFindById() {
        OrderRepository repo = createRepository();

        LocalDateTime now = LocalDateTime.of(2026, 11, 4, 10, 0, 0);
        List<Order.OrderLine> lines = List.of(
                new Order.OrderLine(Sku.of("SERUM_VITC_30"), 2, Money.of(450_000L), false),
                new Order.OrderLine(Sku.of("MAU_THU_5ML"), 1, Money.ZERO, true)
        );

        Order order = new Order("ORD-CONTRACT-1", "customer-1", now, OrderStatus.DA_XAC_NHAN,
                Money.of(900_000L), Money.ZERO, "KHONG_CO", lines);

        repo.save(order);

        Optional<Order> loadedOpt = repo.findById("ORD-CONTRACT-1");
        assertTrue(loadedOpt.isPresent());

        Order loaded = loadedOpt.get();
        assertEquals("ORD-CONTRACT-1", loaded.id());
        assertEquals("customer-1", loaded.customerId());
        assertEquals("DA_XAC_NHAN", loaded.status());
        assertEquals(Money.of(900_000L), loaded.total());
        assertEquals(Money.ZERO, loaded.shippingFee());
        assertEquals(2, loaded.lines().size());
    }

    @Test
    @DisplayName("Tìm đơn hàng không tồn tại trả về Optional.empty")
    void findNonExistentReturnsEmpty() {
        OrderRepository repo = createRepository();
        assertTrue(repo.findById("NON_EXISTENT").isEmpty());
    }
}
