package starter.dathang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import starter.app.AppContainer;
import starter.dathang.adapter.DatHangController;
import starter.domain.Money;
import starter.domain.Product;
import starter.domain.Sku;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("tracer-bullet")
@DisplayName("Lát cắt dọc Tracer Bullet: HTTP Controller -> UseCase -> Domain -> H2 Database")
class DatHangTracerBulletTest {
    private AppContainer container;
    private DatHangController controller;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        // Khởi tạo container dùng cơ sở dữ liệu H2 thực sự
        container = AppContainer.createProductionWithH2("jdbc:h2:mem:tracer_bullet;DB_CLOSE_DELAY=-1");

        // Seed product & inventory vào cơ sở dữ liệu / repository
        LocalDate today = LocalDate.of(2026, 11, 4);
        container.productRepository().save(new Product(
                Sku.of("SERUM_VITC_30"), "Serum Vitamin C 30ml",
                Money.of(450_000L), today.plusYears(1), false
        ));
        container.inventoryRepository().reserve("SERUM_VITC_30", -10); // 10 items in stock

        // Khởi chạy HTTP Server trên port ngẫu nhiên
        controller = new DatHangController(container.datHangUseCase(), 0);
        controller.start();
        port = controller.getPort();
    }

    @AfterEach
    void tearDown() {
        if (controller != null) {
            controller.stop();
        }
    }

    @Test
    @DisplayName("Gửi HTTP POST /api/orders đặt hàng thành công, dữ liệu lưu vào H2 và trả mã 201")
    void tracerBulletHttpOrderToH2() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String jsonPayload = """
                {
                    "customerId": "cust-tracer",
                    "role": "customer",
                    "verified": true,
                    "items": [
                        {"sku": "SERUM_VITC_30", "quantity": 2}
                    ],
                    "shippingData": {
                        "recipientName": "Nguyễn Văn An",
                        "phone": "0912345678",
                        "address": "123 Đường Mỹ Phẩm",
                        "city": "TP. Hồ Chí Minh"
                    },
                    "voucherCode": "KHONG_DUNG",
                    "paymentMethod": "COD",
                    "giftSku": "KHONG_CO"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"success\":true"));
        assertTrue(response.body().contains("\"orderStatus\":\"DA_XAC_NHAN\""));
        assertTrue(response.body().contains("\"totalAmount\":900000"));

        // Kiểm tra tồn kho khả dụng giảm còn 8
        assertEquals(8, container.inventoryRepository().stockOf("SERUM_VITC_30"));
    }
}
