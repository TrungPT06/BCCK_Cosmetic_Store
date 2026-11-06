package starter.stepdefinitions;

import io.cucumber.java.en.*;
import starter.adapter.KiemTraDonHangPresenter;
import starter.entity.OrderLifecyclePolicy;
import starter.infrastructure.InMemoryOrderManagementRepository;
import starter.usecase.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class KiemTraDonHangStepDefinitions {
    private final InMemoryOrderManagementRepository repo = new InMemoryOrderManagementRepository();
    private final KiemTraDonHangPresenter presenter = new KiemTraDonHangPresenter();
    private String role, userId = "an", orderOwner, currentStatus, paymentStatus, action, targetStatus, reasonInput, evidence, sealStatus;
    private int daysSinceDelivered, returnWindowDays = 7;
    private double totalAmount;
    private KiemTraDonHangResponse response;
    private KiemTraDonHangPresenter.KiemTraDonHangViewModel viewModel;
    /* search fields */
    private String filterOrderId, filterCustomer, filterStatus, filterDateRange;
    private int pageSize;

    public KiemTraDonHangStepDefinitions() { }

    private int parseDays(String s) { return "KHONG_AP_DUNG".equals(s) ? 0 : Integer.parseInt(s); }
    private String na(String s) { return "KHONG_AP_DUNG".equals(s) ? null : s; }

    @Given("người dùng T có vai trò {word} và đơn của khách an đang ở trạng thái {word}, tổng tiền {int}, tình trạng thanh toán {word}")
    public void validOrderSetup(String role, String status, int total, String payment) {
        this.role = role; this.userId = "an"; this.orderOwner = "an"; this.currentStatus = status; this.totalAmount = total; this.paymentStatus = payment;
    }

    @Given("đơn được giao cách đây {word} ngày với thời hạn đổi trả {int} ngày và sản phẩm đang ở tình trạng {word}")
    public void deliveryContext(String days, int window, String seal) {
        this.daysSinceDelivered = parseDays(days); this.returnWindowDays = window; this.sealStatus = seal;
    }

    @When("T thực hiện thao tác {word} sang trạng thái {word} với lý do {word} và bằng chứng {word}")
    public void executeAction(String action, String target, String reason, String evidence) {
        this.action = action; this.targetStatus = na(target); this.reasonInput = reason; this.evidence = na(evidence);
        var request = KiemTraDonHangRequest.action(role, userId, orderOwner, currentStatus, paymentStatus, totalAmount,
                action, this.targetStatus, reasonInput, this.evidence, daysSinceDelivered, returnWindowDays, sealStatus);
        response = new KiemTraDonHangUseCase(repo, new OrderLifecyclePolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = LocalDate.of(2026, 11, 4);
        world.refundAmount = response.refundAmount(); world.stockReleased = response.stockReleased();
    }

    @Then("T được thông báo trạng thái đơn mới là {word} với màu {word}")
    public void statusResult(String status, String color) {
        assertTrue(response.success()); assertEquals(status, response.newStatus()); assertEquals(color, viewModel.color());
    }

    @Then("lịch sử trạng thái được ghi thêm khi trạng thái thay đổi")
    public void historyRecorded() { if (response.statusChanged()) assertTrue(repo.hasHistory()); }

    @Then("trạng thái đơn không thay đổi và không có thay đổi nào được lưu lại")
    public void noChange() { assertFalse(response.stored()); }

    /* --- Invalid scenario (different owner) --- */
    @Given("người dùng T là khách an có vai trò {word} và đơn thuộc về khách {word} đang ở trạng thái {word}")
    public void invalidOwnerSetup(String role, String owner, String status) {
        this.role = role; this.userId = "an"; this.orderOwner = owner; this.currentStatus = status;
        this.paymentStatus = "DA_THANH_TOAN"; this.totalAmount = 650_000;
    }

    /* --- Search scenario --- */
    @Given("người dùng T có vai trò admin và hệ thống có {int} đơn mẫu tạo trong tháng {word} gồm {int} đơn DA_XAC_NHAN của an, {int} đơn DA_XAC_NHAN của binh và {int} đơn DA_GIAO của binh, trong đó DH001 là đơn của an")
    public void searchSetup(int total, String month, int anConfirmed, int binhConfirmed, int binhDelivered) {
        this.role = "admin"; this.userId = "admin";
        List<OrderManagementRepository.ManagedOrder> samples = new ArrayList<>();
        LocalDate base = LocalDate.of(2026, 11, 1);
        for (int i = 0; i < anConfirmed; i++)
            samples.add(new OrderManagementRepository.ManagedOrder(i == 0 ? "DH001" : "DH_AN_" + i, "an", "DA_XAC_NHAN", "DA_THANH_TOAN", 650_000, base.plusDays(i)));
        for (int i = 0; i < binhConfirmed; i++)
            samples.add(new OrderManagementRepository.ManagedOrder("DH_BINH_C_" + i, "binh", "DA_XAC_NHAN", "DA_THANH_TOAN", 650_000, base.plusDays(i)));
        for (int i = 0; i < binhDelivered; i++)
            samples.add(new OrderManagementRepository.ManagedOrder("DH_BINH_D_" + i, "binh", "DA_GIAO", "DA_THANH_TOAN", 650_000, base.plusDays(i)));
        repo.addSampleOrders(samples);
    }

    @When("T thực hiện thao tác TIM_KIEM_DON_HANG theo mã đơn {word}, khách hàng {word}, trạng thái {word} và khoảng ngày {word} với kích thước trang {int}")
    public void searchAction(String fId, String fCust, String fStatus, String fDate, int page) {
        var request = new KiemTraDonHangRequest(role, userId, userId, null, null, 0,
                "TIM_KIEM_DON_HANG", null, null, null, 0, 0, null,
                fId, fCust, fStatus, fDate, page);
        response = new KiemTraDonHangUseCase(repo, new OrderLifecyclePolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = LocalDate.of(2026, 11, 4);
    }

    @Then("T được thông báo có {int} đơn thỏa điều kiện chia thành {int} trang với màu {word}")
    public void searchResult(int matched, int pages, String color) {
        assertTrue(response.success()); assertEquals(matched, response.matchedCount()); assertEquals(pages, response.totalPages()); assertEquals(color, viewModel.color());
    }
}
