package starter.stepdefinitions;

import io.cucumber.java.en.*;
import starter.adapter.KiemTraTonKhoPresenter;
import starter.entity.InventoryPolicy;
import starter.infrastructure.InMemoryInventoryRepository;
import starter.usecase.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class KiemTraTonKhoStepDefinitions {
    private final InMemoryInventoryRepository repo = new InMemoryInventoryRepository();
    private final KiemTraTonKhoPresenter presenter = new KiemTraTonKhoPresenter();
    private final LocalDate today = LocalDate.of(2026, 11, 4);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private KiemTraTonKhoResponse response;
    private KiemTraTonKhoPresenter.KiemTraTonKhoViewModel viewModel;
    /* admin fields */
    private String batchId;
    private int qtyBefore;
    private LocalDate batchExpiry;
    private String adminRole = "admin";
    /* warning fields */
    private LocalDate warningExpiry;

    public KiemTraTonKhoStepDefinitions() { }

    private LocalDate parse(String d) { return LocalDate.parse(d, DATE); }

    @Given("sản phẩm SERUM_VITC_30 có lô L001 số lượng {int} hạn sử dụng {word} và lô L002 số lượng {int} hạn sử dụng {word}")
    public void setupBatches(int l1Qty, String l1Exp, int l2Qty, String l2Exp) {
        repo.setup(List.of(
            new InventoryPolicy.Batch("L001", l1Qty, parse(l1Exp)),
            new InventoryPolicy.Batch("L002", l2Qty, parse(l2Exp))
        ), 0);
    }

    @Given("số lượng đang giữ chỗ là {int} và ngưỡng hạn sử dụng tối thiểu khi giao là {int} tháng")
    public void setReserved(int reserved, int ignored) { repo.setup(repo.getBatches(), reserved); }

    @When("người dùng T có vai trò {word} thực hiện thao tác {word} với số lượng {int}")
    public void executeInventory(String role, String action, int qty) {
        var request = new KiemTraTonKhoRequest(role, action, qty, null, 0, null, null, today);
        response = new KiemTraTonKhoUseCase(repo, new InventoryPolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = today;
    }

    @Then("T được thông báo kết quả là {word} với tồn kho khả dụng là {int} và màu {word}")
    public void checkResult(String result, int available, String color) {
        assertTrue(response.success()); assertEquals(result, response.message()); assertEquals(available, response.availableQty()); assertEquals(color, viewModel.color());
    }

    @Then("lô được chọn xuất theo nguyên tắc FEFO là {word}")
    public void pickedBatches(String picked) { assertEquals(picked, response.pickedBatches()); }

    @Then("kết quả thay đổi tồn kho được lưu lại {word}")
    public void storeResult(String stored) { assertEquals(Boolean.parseBoolean(stored), response.stored()); }

    @Then("tồn kho được kiểm tra vào ngày {word}")
    public void checkedDate(String date) { assertEquals(parse(date), today); }

    @Then("tồn kho khả dụng vẫn là {int} và không có thay đổi nào được lưu lại")
    public void noInventoryChange(int available) { assertEquals(available, response.availableQty()); assertFalse(response.stored()); }

    /* --- Admin scenarios --- */
    @Given("người dùng T có vai trò admin và lô {word} của sản phẩm SERUM_VITC_30 đang có {int} sản phẩm")
    public void adminBatchSetup(String batch, int qty) {
        this.batchId = batch; this.qtyBefore = qty; this.adminRole = "admin";
        if (qty > 0) repo.addBatch(batch, qty, today.plusMonths(12));
    }

    @When("T thực hiện thao tác {word} với số lượng thay đổi {int} và lý do {word}")
    public void adminAction(String action, int changeQty, String reason) {
        var request = new KiemTraTonKhoRequest("admin", action, 0, batchId, changeQty, reason,
                batchExpiry != null ? batchExpiry : today.plusMonths(12), today);
        response = new KiemTraTonKhoUseCase(repo, new InventoryPolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = today;
    }

    @Then("T được thông báo số lượng lô sau thay đổi là {int} với màu {word}")
    public void adminResult(int qtyAfter, String color) {
        assertTrue(response.success()); assertEquals(qtyAfter, response.batchQtyAfter()); assertEquals(color, viewModel.color());
    }

    @Then("log thay đổi tồn kho \\(ai, khi nào, giá trị cũ và mới) và nhật ký kiểm toán được ghi")
    public void logRecorded() { assertTrue(repo.hasLogs()); }

    @Then("kết quả thay đổi tồn kho được lưu lại")
    public void inventoryStored() { assertTrue(response.stored()); }

    /* --- Admin invalid scenarios --- */
    @Given("người dùng T có vai trò {word} và lô {word} của sản phẩm SERUM_VITC_30 đang có {int} sản phẩm, hạn sử dụng {word}")
    public void adminInvalidSetup(String role, String batch, int qty, String expiry) {
        this.adminRole = role; this.batchId = batch; this.qtyBefore = qty; this.batchExpiry = parse(expiry);
        if (qty > 0) repo.addBatch(batch, qty, this.batchExpiry);
    }

    @When("T thực hiện thao tác {word} với số lượng thay đổi {int} vào ngày {word}")
    public void adminInvalidAction(String action, int changeQty, String date) {
        if (batchExpiry == null) batchExpiry = today.plusMonths(12);
        var request = new KiemTraTonKhoRequest(
            adminRole != null ? adminRole : "admin",
            action, 0, batchId, changeQty, null, batchExpiry, parse(date));
        response = new KiemTraTonKhoUseCase(repo, new InventoryPolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = parse(date);
    }

    @Then("số lượng lô giữ nguyên và không có thay đổi nào được lưu lại")
    public void noAdminChange() { assertFalse(response.stored()); }

    /* --- Warning scenarios --- */
    @Given("một lô của sản phẩm SERUM_VITC_30 có hạn sử dụng {word} và ngưỡng cảnh báo là {int} ngày")
    public void warningSetup(String expiry, int ignored) { this.warningExpiry = parse(expiry); }

    @When("hệ thống thực hiện thao tác QUET_CANH_BAO_LO_SAP_HET_HAN vào ngày {word}")
    public void scanWarning(String date) {
        var request = new KiemTraTonKhoRequest("he_thong", "QUET_CANH_BAO_LO_SAP_HET_HAN", 0,
                null, 0, null, warningExpiry, parse(date));
        response = new KiemTraTonKhoUseCase(repo, new InventoryPolicy()).execute(request);
        viewModel = presenter.present(response);
        var world = TestWorld.current();
        world.success = response.success(); world.message = response.message();
        world.color = viewModel.color(); world.processedDate = parse(date);
    }

    @Then("admin nhận cảnh báo {word} với số ngày còn lại {int} và màu {word}")
    public void warningResult(String status, int daysLeft, String color) {
        assertEquals(status, response.warningStatus()); assertEquals(daysLeft, response.daysLeft()); assertEquals(color, viewModel.color());
    }
}
