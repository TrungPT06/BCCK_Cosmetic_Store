package starter.stepdefinitions;

import io.cucumber.java.en.*;
import starter.adapter.DatHangPresenter;
import starter.entity.*;
import starter.infrastructure.InMemoryOrderRepository;
import starter.infrastructure.InMemoryProductRepository;
import starter.infrastructure.InMemoryVoucherRepository;
import starter.usecase.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

public class DatHangStepDefinitions {
    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final InMemoryProductRepository productRepository = new InMemoryProductRepository();
    private final InMemoryVoucherRepository voucherRepository = new InMemoryVoucherRepository();
    private final DatHangPresenter presenter = new DatHangPresenter();
    private String role, sku, shippingInfo = "HOP_LE", voucher = "KHONG_DUNG", paymentMethod, giftSku = "KHONG_CO";
    private boolean verified = true; private int quantity, stock; private double unitPrice, discount; private LocalDate expiry, today;
    private DatHangResponse response;
    private DatHangPresenter.DatHangViewModel viewModel;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Given("người dùng T có vai trò {word} đã xác thực tài khoản")
    public void authenticated(String role) { this.role = role; this.verified = true; }

    @Given("người dùng T có vai trò {word} đã xác thực tài khoản là {word}")
    public void account(String role, String verified) { this.role = role; this.verified = Boolean.parseBoolean(verified); }

    @Given("giỏ hàng của T có {int} sản phẩm {word} với đơn giá {double}, còn hạn sử dụng trên 6 tháng và tồn kho khả dụng {int}")
    public void validCart(int qty, String sku, double price, int stock) { setupCart(qty, sku, price, stock, null); }

    @Given("giỏ hàng của T có {int} sản phẩm {word} với đơn giá {double}, hạn sử dụng {word} và tồn kho khả dụng {int}")
    public void cart(int qty, String sku, double price, String expiry, int stock) { setupCart(qty, sku, price, stock, expiry); }

    @Given("thông tin giao hàng của T hợp lệ và khu vực giao hàng có phí vận chuyển {int}")
    public void validShipping(int ignored) { shippingInfo = "HOP_LE"; }

    @Given("thông tin giao hàng của T là {word}")
    public void shipping(String info) { shippingInfo = info; }

    @Given("T áp dụng voucher {word} có giá trị giảm {double}")
    public void voucher(String voucher, double discount) {
        this.voucher = voucher;
        this.discount = discount;
        seedVoucher(voucher, discount);
    }

    @Given("T áp dụng voucher {word}")
    public void voucherOnly(String voucher) {
        this.voucher = voucher;
        if ("GIAM50K".equalsIgnoreCase(voucher)) {
            this.discount = 50_000.0;
        } else if ("GIAM500K".equalsIgnoreCase(voucher)) {
            this.discount = 500_000.0;
        } else {
            this.discount = 0.0;
        }
        seedVoucher(voucher, this.discount);
    }

    @When("T thực hiện thao tác DAT_HANG với phương thức thanh toán {word}")
    @When("T đặt hàng với phương thức thanh toán {word}")
    @When("T đặt đơn hàng với phương thức thanh toán {word}")
    public void order(String payment) {
        paymentMethod = payment;
        // Thực thi Use Case và gửi dữ liệu qua Output Boundary (Presenter)
        response = useCase().execute(request());
        viewModel = presenter.getViewModel();
        var world = TestWorld.current();
        world.success = response.success();
        world.message = response.message();
        world.color = viewModel.color();
        world.processedDate = today;
    }

    @Then("T được thông báo trạng thái đơn hàng là {word} với tổng thanh toán là {double} và màu {word}")
    public void success(String status, double total, String color) {
        assertTrue(response.success());
        assertEquals(status, response.orderStatus());
        assertEquals(total, response.totalAmount());
        assertEquals(color, viewModel.color());
    }

    @Then("T được thông báo trạng thái đơn hàng là {word} với tổng thanh toán là {double}")
    public void successWithoutColor(String status, double total) {
        assertTrue(response.success());
        assertEquals(status, response.orderStatus());
        assertEquals(total, response.totalAmount());
    }

    @Then("phí vận chuyển áp dụng là {double} và tồn kho khả dụng của sản phẩm {word} còn {int}")
    public void inventory(double fee, String ignored, int remaining) {
        assertEquals(fee, response.shippingFee());
        assertEquals(remaining, response.remainingStock());
    }

    @Then("đơn hàng được lưu lại và sản phẩm {word} được xóa khỏi giỏ hàng")
    public void saved(String ignored) {
        assertTrue(response.stored());
        assertTrue(repository.containsOrder());
    }

    @Then("không có đơn hàng nào được tạo, tồn kho khả dụng và giỏ hàng giữ nguyên")
    public void unchanged() {
        assertFalse(repository.containsOrder());
        assertEquals(stock, repository.stockOf(sku));
    }

    @Given("người dùng T có vai trò customer có {int} sản phẩm {word} trong giỏ hàng hợp lệ")
    public void giftCart(int qty, String sku) {
        role = "customer";
        verified = true;
        shippingInfo = "HOP_LE";
        voucher = "KHONG_DUNG";
        setupCart(qty, sku, 450_000, 10, null);
    }

    @Given("chương trình tặng quà {word} có tồn kho riêng là {int}")
    public void gift(String sku, int stock) {
        giftSku = sku;
        if (!"KHONG_CO".equals(sku)) {
            repository.setStock(sku, stock);
            productRepository.save(Product.gift(sku, "Quà tặng " + sku));
        }
    }

    @Then("đơn hàng được gắn cờ {word} với dòng quà tặng có đơn giá {word}")
    public void flag(String flag, String price) {
        assertEquals(flag, response.orderFlag());
        if (!"KHONG_AP_DUNG".equals(price)) {
            assertEquals(Double.parseDouble(price), response.giftLinePrice());
        }
    }

    @Then("tồn kho riêng của quà tặng còn {int} và đơn hàng được lưu lại")
    public void giftStock(int remaining) {
        assertEquals(remaining, response.remainingGiftStock());
        assertTrue(response.stored());
    }

    private void setupCart(int qty, String sku, double price, int stock, String expiryText) {
        this.quantity = qty;
        this.sku = sku;
        this.unitPrice = price;
        this.stock = stock;
        repository.setStock(sku, stock);
        today = LocalDate.of(2026, 11, 4);
        expiry = expiryText == null ? today.plusMonths(7) : ("KHONG_AP_DUNG".equals(expiryText) ? null : parse(expiryText));
        if (sku != null && !"KHONG_CO".equals(sku)) {
            productRepository.save(Product.regular(sku, "Sản phẩm " + sku, price, expiry));
        }
    }

    private void seedVoucher(String code, double discountAmount) {
        if (code != null && !"KHONG_DUNG".equalsIgnoreCase(code)) {
            voucherRepository.save(Voucher.from(code, discountAmount, today != null ? today : LocalDate.of(2026, 11, 4)));
        }
    }

    private DatHangUseCase useCase() {
        return new DatHangUseCase(repository, new OrderPolicy(), new ShippingPolicy(), new PricingPolicy(),
                new ProductClassification(), presenter, productRepository, voucherRepository, new OrderFactory());
    }

    private DatHangRequest request() {
        return new DatHangRequest(role, verified, sku, quantity, unitPrice, expiry, shippingInfo, voucher, discount, paymentMethod, today, giftSku);
    }

    private LocalDate parse(String date) {
        return LocalDate.parse(date, DATE);
    }
}
