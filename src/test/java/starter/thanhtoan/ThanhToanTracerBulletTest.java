package starter.thanhtoan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import starter.app.AppContainer;
import starter.domain.Money;
import starter.domain.Order;
import starter.domain.OrderStatus;
import starter.domain.Sku;
import starter.thanhtoan.adapter.ThanhToanController;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tracer Bullet của lát cắt Thanh toán:
 * HTTP JSON -> ThanhToanController -> use case -> Order (domain) -> JdbcOrderRepository (H2) -> Presenter -> HTTP.
 */
@Tag("tracer-bullet")
@DisplayName("Tracer Bullet: lát cắt dọc Thanh toán xuyên suốt tới H2")
class ThanhToanTracerBulletTest {

    private AppContainer container;
    private ThanhToanController controller;

    @BeforeEach
    void setUp() {
        container = AppContainer.createProductionWithH2("jdbc:h2:mem:tt_tracer_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        controller = container.thanhToanController();
        container.orderRepository().save(new Order("ORD-TT-1", "cust-1", LocalDateTime.now(),
                OrderStatus.CHO_THANH_TOAN, Money.of(700_000L), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(700_000L), false))));
    }

    @Test
    @DisplayName("Thanh toán online thành công: đơn chuyển DA_XAC_NHAN và được lưu xuống H2")
    void onlinePaymentEndToEnd() {
        var response = controller.handle(ThanhToanController.HttpRequest.post("/api/payments", """
                {"orderId":"ORD-TT-1","userId":"cust-1","role":"customer","action":"THANH_TOAN_ONLINE",
                 "gatewayResult":"THANH_CONG","gatewayAmount":700000,"signature":"HOP_LE"}"""));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"orderStatus\":\"DA_XAC_NHAN\""));
        assertTrue(response.body().contains("\"paymentStatus\":\"DA_THANH_TOAN\""));
        assertEquals(OrderStatus.DA_XAC_NHAN, container.orderRepository().findById("ORD-TT-1").get().orderStatus());
    }

    @Test
    @DisplayName("Thiếu trường bắt buộc: trả 400 và không đổi đơn hàng")
    void missingFieldReturns400() {
        var response = controller.handle(ThanhToanController.HttpRequest.post("/api/payments",
                "{\"orderId\":\"ORD-TT-1\",\"role\":\"customer\",\"action\":\"THANH_TOAN_ONLINE\"}"));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("THIEU_TRUONG_BAT_BUOC_userId"));
        assertEquals(OrderStatus.CHO_THANH_TOAN, container.orderRepository().findById("ORD-TT-1").get().orderStatus());
    }

    @Test
    @DisplayName("Hủy đơn qua HTTP: đơn chuyển DA_HUY")
    void cancelEndToEnd() {
        var response = controller.handle(ThanhToanController.HttpRequest.post("/api/payments",
                "{\"orderId\":\"ORD-TT-1\",\"userId\":\"cust-1\",\"role\":\"customer\",\"action\":\"HUY_DON\"}"));

        assertEquals(200, response.statusCode());
        assertEquals(OrderStatus.DA_HUY, container.orderRepository().findById("ORD-TT-1").get().orderStatus());
    }
}
