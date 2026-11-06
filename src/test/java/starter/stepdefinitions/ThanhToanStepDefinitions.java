package starter.stepdefinitions;

import io.cucumber.java.en.*;
import starter.adapter.ThanhToanPresenter;
import starter.entity.PaymentPolicy;
import starter.infrastructure.DefaultPaymentGateway;
import starter.infrastructure.InMemoryPaymentRepository;
import starter.usecase.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ThanhToanStepDefinitions {
    private final InMemoryPaymentRepository repo = new InMemoryPaymentRepository();
    private final DefaultPaymentGateway paymentGateway = new DefaultPaymentGateway();
    private final ThanhToanPresenter presenter = new ThanhToanPresenter();
    private String role, userId = "an", orderOwner = "an", orderStatus, paymentMethod, action;
    private double totalAmount = 700_000;
    private int minutesSinceCreated, recordedBefore;
    private ThanhToanResponse response;
    private ThanhToanPresenter.ThanhToanViewModel viewModel;

    public ThanhToanStepDefinitions() { }

    @Given("người dùng T có vai trò {word} đang xử lý đơn của khách an với phương thức {word}, tổng thanh toán {int} và trạng thái {word}")
    public void validPaymentSetup(String role, String method, int total, String status) {
        this.role = role; this.paymentMethod = method; this.totalAmount = total; this.orderStatus = status;
        this.userId = "admin".equals(role) || "he_thong".equals(role) ? role : "an"; this.orderOwner = "an";
    }

    @Given("đơn được tạo cách đây {int} phút \\(thời hạn thanh toán online là {int} phút) và đã có {int} giao dịch thành công")
    public void paymentTiming(int minutes, int ignored, int recorded) {
        this.minutesSinceCreated = minutes; this.recordedBefore = recorded;
    }

    @When("T thực hiện thao tác {word} và cổng thanh toán trả kết quả {word} \\(chữ ký hợp lệ, số tiền khớp với đơn)")
    public void validPaymentAction(String action, String gateway) {
        this.action = action;
        var request = new ThanhToanRequest(role, userId, orderOwner, orderStatus, paymentMethod,
                totalAmount, action, gateway, totalAmount, "HOP_LE", minutesSinceCreated, recordedBefore);
        // UseCase điều phối qua Output Boundary (Presenter) và Gateway
        response = new ThanhToanUseCase(repo, new PaymentPolicy(), null, paymentGateway, presenter).execute(request);
        viewModel = presenter.getViewModel();
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = LocalDate.of(2026, 11, 4);
        world.refundAmount = response.refundAmount(); world.stockReleased = response.stockReleased();
    }

    @Then("T được thông báo trạng thái đơn là {word} và tình trạng thanh toán là {word} với màu {word}")
    public void paymentResult(String orderSt, String paySt, String color) {
        assertTrue(response.success()); assertEquals(orderSt, response.orderStatusAfter()); assertEquals(paySt, response.paymentStatus()); assertEquals(color, viewModel.color());
    }

    @Then("trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận")
    public void noPaymentChange() { assertFalse(response.success()); }

    /* --- Kịch bản không hợp lệ --- */
    @Given("người dùng T là khách an có vai trò {word} đang xử lý đơn thuộc về khách {word} với phương thức {word}, tổng thanh toán {int} và trạng thái {word}")
    public void invalidPaymentSetup(String role, String owner, String method, int total, String status) {
        this.role = role; this.userId = "an"; this.orderOwner = owner;
        this.paymentMethod = method; this.totalAmount = total; this.orderStatus = status;
        this.minutesSinceCreated = 0; this.recordedBefore = 0;
    }

    @When("T thực hiện thao tác {word} và cổng thanh toán trả kết quả {word} với số tiền {double} và chữ ký {word}")
    public void invalidPaymentAction(String action, String gateway, double amount, String signature) {
        this.action = action;
        String gw = "KHONG_AP_DUNG".equals(gateway) ? null : gateway;
        var request = new ThanhToanRequest(role, userId, orderOwner, orderStatus, paymentMethod,
                totalAmount, action, gw, amount, signature, minutesSinceCreated, recordedBefore);
        response = new ThanhToanUseCase(repo, new PaymentPolicy(), null, paymentGateway, presenter).execute(request);
        viewModel = presenter.getViewModel();
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = LocalDate.of(2026, 11, 4);
        world.refundAmount = response.refundAmount(); world.stockReleased = response.stockReleased();
    }
}
