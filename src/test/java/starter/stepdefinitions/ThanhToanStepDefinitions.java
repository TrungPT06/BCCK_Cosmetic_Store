package starter.stepdefinitions;

import io.cucumber.java.en.*;
import starter.dathang.usecase.InventoryRepository;
import starter.dathang.usecase.OrderRepository;
import starter.domain.*;
import starter.infrastructure.FakePaymentGateway;
import starter.infrastructure.InMemoryInventoryRepository;
import starter.infrastructure.InMemoryOrderRepository;
import starter.infrastructure.InMemoryPaymentRepository;
import starter.thanhtoan.adapter.ThanhToanPresenter;
import starter.thanhtoan.usecase.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThanhToanStepDefinitions {
    private final OrderRepository orderRepository = new InMemoryOrderRepository();
    private final InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
    private final PaymentRepository paymentRepository = new InMemoryPaymentRepository();
    private final FakePaymentGateway paymentGateway = new FakePaymentGateway();
    private final ThanhToanPresenter presenter = new ThanhToanPresenter();

    private LocalDate today = LocalDate.of(2026, 11, 4);
    private Clock clock = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private String role = "customer";
    private String userId = "an";
    private String orderOwner = "an";
    private String orderStatusBefore = "CHO_THANH_TOAN";
    private String paymentMethod = "VNPAY";
    private long totalAmount = 700_000L;
    private int minutesSinceCreated = 5;
    private int recordedBefore = 0;
    private String orderId = "ORD_TEST_1";

    private ThanhToanPresenter.ThanhToanViewModel viewModel;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private ThanhToanOnlineUseCase onlineUseCase() {
        return new ThanhToanOnlineUseCase(orderRepository, paymentRepository, paymentGateway, presenter, clock);
    }

    private XacNhanThuTienCodUseCase codUseCase() {
        return new XacNhanThuTienCodUseCase(orderRepository, paymentRepository, presenter, clock);
    }

    private HuyDonUseCase huyDonUseCase() {
        return new HuyDonUseCase(orderRepository, inventoryRepository, paymentRepository, presenter, clock);
    }

    @Given("hệ thống xử lý thanh toán vào ngày {word}")
    public void setupDate(String dateStr) {
        this.today = LocalDate.parse(dateStr, DATE_FORMAT);
        this.clock = Clock.fixed(today.atTime(10, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(), ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @Given("người dùng T có vai trò {word} đang xử lý đơn của khách an với phương thức {word}, tổng thanh toán {long} và trạng thái {word}")
    public void validOrderSetup(String role, String method, long total, String status) {
        this.role = role;
        this.paymentMethod = method;
        this.totalAmount = total;
        this.orderStatusBefore = status;
        this.orderOwner = "an";
        this.userId = ("admin".equalsIgnoreCase(role) || "he_thong".equalsIgnoreCase(role)) ? role : "an";
    }

    @Given("đơn được tạo cách đây {int} phút và đã có {int} giao dịch thành công")
    public void orderCreatedMinutesAgo(int minutes, int recorded) {
        this.minutesSinceCreated = minutes;
        this.recordedBefore = recorded;

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime createdAt = now.minusMinutes(minutes);

        inventoryRepository.reserve("SERUM_VITC_30", -10); // stock = 10

        Order order = new Order(orderId, orderOwner, createdAt, OrderStatus.valueOf(orderStatusBefore),
                Money.of(totalAmount), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(totalAmount), false)));
        orderRepository.save(order);

        for (int i = 0; i < recorded; i++) {
            paymentRepository.recordTransaction(orderId, "THANH_TOAN_ONLINE", "DA_THANH_TOAN");
        }
    }

    @When("T thực hiện thao tác {word} và cổng thanh toán trả kết quả {word} với chữ ký hợp lệ và số tiền khớp với đơn")
    public void executeValidAction(String action, String gatewayResult) {
        Money gwAmount = Money.of(totalAmount);
        String gw = "KHONG_AP_DUNG".equalsIgnoreCase(gatewayResult) ? null : gatewayResult;
        var request = new ThanhToanRequest(orderId, userId, role, action, gw, gwAmount, "HOP_LE");

        dispatch(action, request);
    }

    @Given("người dùng T là khách an có vai trò {word} đang xử lý đơn thuộc về khách {word} với phương thức {word}, tổng thanh toán {long} và trạng thái {word}")
    public void invalidOrderSetup(String role, String owner, String method, long total, String status) {
        this.role = role;
        this.userId = "an";
        this.orderOwner = owner;
        this.paymentMethod = method;
        this.totalAmount = total;
        this.orderStatusBefore = status;

        LocalDateTime now = LocalDateTime.now(clock);
        Order order = new Order(orderId, owner, now, OrderStatus.valueOf(status),
                Money.of(total), Money.ZERO, "KHONG_CO",
                List.of(new Order.OrderLine(Sku.of("SERUM_VITC_30"), 1, Money.of(total), false)));
        orderRepository.save(order);
    }

    @When("T thực hiện thao tác {word} và cổng thanh toán trả kết quả {word} với số tiền {long} và chữ ký {word}")
    public void executeInvalidAction(String action, String gatewayResult, long amount, String signature) {
        String gw = "KHONG_AP_DUNG".equalsIgnoreCase(gatewayResult) ? null : gatewayResult;
        String sig = "KHONG_AP_DUNG".equalsIgnoreCase(signature) ? null : signature;
        var request = new ThanhToanRequest(orderId, userId, role, action, gw, Money.of(amount), sig);

        dispatch(action, request);
    }

    private void dispatch(String action, ThanhToanRequest request) {
        switch (action) {
            case "THANH_TOAN_ONLINE", "THANH_TOAN_LAI_DOI_PHUONG_THUC" -> onlineUseCase().execute(request);
            case "XAC_NHAN_THU_TIEN_COD" -> codUseCase().execute(request);
            case "HUY_QUA_HAN_THANH_TOAN", "HUY_DON" -> huyDonUseCase().execute(request);
            default -> throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
        }

        this.viewModel = presenter.getViewModel();
        TestWorld world = TestWorld.current();
        world.success = viewModel.success();
        world.message = viewModel.message();
        world.processedDate = viewModel.processedDate();
        world.refundAmount = viewModel.refundAmount();
        world.stockReleased = viewModel.stockReleased();
    }

    @Then("T được thông báo trạng thái đơn là {word} và tình trạng thanh toán là {word}")
    public void verifyResult(String expectedOrderStatus, String expectedPaymentStatus) {
        assertTrue(viewModel.success());
        assertEquals(expectedOrderStatus, viewModel.orderStatusAfter());
        assertEquals(expectedPaymentStatus, viewModel.paymentStatus());
    }

    @Then("trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận")
    public void verifyNoChangeAndNoTransaction() {
        assertFalse(viewModel.success());
        assertEquals(orderStatusBefore, orderRepository.findById(orderId).get().status());
        assertEquals(0, paymentRepository.transactionCount(orderId));
    }
}
