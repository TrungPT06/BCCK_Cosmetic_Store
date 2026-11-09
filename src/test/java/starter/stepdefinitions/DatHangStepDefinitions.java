package starter.stepdefinitions;

import starter.shared.port.*;
import io.cucumber.java.en.*;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatHangStepDefinitions {
    private final InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
    private final InMemoryProductRepository productRepository = new InMemoryProductRepository();
    private final InMemoryVoucherRepository voucherRepository = new InMemoryVoucherRepository();
    private final InMemoryInventoryRepository inventoryRepository = new InMemoryInventoryRepository();
    private final DatHangPresenter presenter = new DatHangPresenter();

    private LocalDate today = LocalDate.of(2026, 11, 4);
    private Clock clock = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private String role = "customer";
    private boolean verified = true;
    private String sku = "SERUM_VITC_30";
    private int quantity = 1;
    private int initialStock = 10;
    private String voucherCode = "KHONG_DUNG";
    private String giftSku = "KHONG_CO";
    private DatHangRequest.ShippingData shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");

    private DatHangPresenter.DatHangViewModel viewModel;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private DatHangUseCase useCase() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        return new DatHangUseCase(
                orderRepository, productRepository, voucherRepository, inventoryRepository,
                new OrderPolicy(factory),
                new OrderFactory(factory),
                new StandardShippingFeeStrategy(),
                presenter,
                clock
        );
    }

    @Given("hệ thống xử lý các giao dịch vào ngày {word}")
    public void systemDate(String dateStr) {
        this.today = LocalDate.parse(dateStr, DATE_FORMAT);
        this.clock = Clock.fixed(today.atTime(10, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(), ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @Given("người dùng T có vai trò {word} đã xác thực tài khoản")
    public void authenticated(String role) {
        this.role = role;
        this.verified = true;
    }

    @Given("người dùng T có vai trò {word} đã xác thực tài khoản là {word}")
    public void accountVerification(String role, String verifiedStr) {
        this.role = role;
        this.verified = Boolean.parseBoolean(verifiedStr);
    }

    @Given("giỏ hàng của T có {int} sản phẩm {word} với đơn giá {long}, còn hạn sử dụng trên 6 tháng và tồn kho khả dụng {int}")
    public void cartWithNearExpiryOver6Months(int qty, String sku, long price, int stock) {
        this.sku = sku;
        this.quantity = qty;
        this.initialStock = stock;
        boolean restricted = sku.startsWith("NUOC_HOA_");
        productRepository.save(new Product(Sku.of(sku), sku, Money.of(price), today.plusYears(1), restricted));
        inventoryRepository.setStock(sku, stock);
    }

    @Given("giỏ hàng của T có {int} sản phẩm {word} với đơn giá {long}, hạn sử dụng {word} và tồn kho khả dụng {int}")
    public void cartWithSpecificExpiry(int qty, String sku, long price, String expiryStr, int stock) {
        this.sku = sku;
        this.quantity = qty;
        this.initialStock = stock;
        LocalDate expiry = "KHONG_AP_DUNG".equalsIgnoreCase(expiryStr) ? today.plusYears(1) : LocalDate.parse(expiryStr, DATE_FORMAT);
        boolean restricted = sku.startsWith("NUOC_HOA_");
        if (!"KHONG_CO".equalsIgnoreCase(sku)) {
            productRepository.save(new Product(Sku.of(sku), sku, Money.of(price), expiry, restricted));
            inventoryRepository.setStock(sku, stock);
        }
    }

    @Given("thông tin giao hàng của T hợp lệ và khu vực giao hàng có phí vận chuyển {int}")
    public void validShipping(int fee) {
        this.shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
    }

    @Given("thông tin giao hàng của T là {word}")
    public void shippingInfo(String info) {
        if ("HOP_LE".equalsIgnoreCase(info)) {
            this.shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
        } else if ("THIEU_DIA_CHI".equalsIgnoreCase(info)) {
            this.shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "", "TP. Hồ Chí Minh");
        } else if ("SDT_KHONG_HOP_LE".equalsIgnoreCase(info)) {
            this.shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "12345", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
        }
    }

    @Given("T áp dụng voucher {word}")
    public void applyVoucher(String voucherCode) {
        this.voucherCode = voucherCode;
        if ("GIAM50K".equalsIgnoreCase(voucherCode)) {
            voucherRepository.save(new Voucher("GIAM50K", Money.of(50_000L), Money.ZERO, today.plusMonths(6), 10));
        } else if ("GIAM500K".equalsIgnoreCase(voucherCode)) {
            voucherRepository.save(new Voucher("GIAM500K", Money.of(500_000L), Money.ZERO, today.plusMonths(6), 10));
        } else if ("HETHAN".equalsIgnoreCase(voucherCode)) {
            voucherRepository.save(new Voucher("HETHAN", Money.of(50_000L), Money.ZERO, today.minusDays(1), 10));
        } else if ("HETLUOT".equalsIgnoreCase(voucherCode)) {
            voucherRepository.save(new Voucher("HETLUOT", Money.of(50_000L), Money.ZERO, today.plusMonths(6), 0));
        } else if ("DONLON".equalsIgnoreCase(voucherCode)) {
            voucherRepository.save(new Voucher("DONLON", Money.of(500_000L), Money.of(10_000_000L), today.plusMonths(6), 10));
        }
    }

    @Given("người dùng T có vai trò customer có {int} sản phẩm {word} trong giỏ hàng hợp lệ")
    public void customerHasValidCart(int qty, String sku) {
        this.role = "customer";
        this.verified = true;
        this.sku = sku;
        this.quantity = qty;
        this.initialStock = 10;
        boolean restricted = sku.startsWith("NUOC_HOA_");
        long price = restricted ? 1_200_000L : 450_000L;
        productRepository.save(new Product(Sku.of(sku), sku, Money.of(price), today.plusYears(1), restricted));
        inventoryRepository.setStock(sku, 10);
        this.shippingData = new DatHangRequest.ShippingData("Nguyễn Văn An", "0912345678", "123 Đường Mỹ Phẩm", "TP. Hồ Chí Minh");
        this.voucherCode = "KHONG_DUNG";
    }

    @Given("chương trình tặng quà {word} có tồn kho riêng là {int}")
    public void giftPromotion(String giftSku, int stock) {
        this.giftSku = giftSku;
        if (!"KHONG_CO".equalsIgnoreCase(giftSku)) {
            productRepository.save(new Product(Sku.of(giftSku), giftSku, Money.ZERO, today.plusYears(1), false));
            inventoryRepository.setStock(giftSku, stock);
        }
    }

    @When("T đặt hàng với phương thức thanh toán {word}")
    public void placeOrder(String paymentMethod) {
        List<DatHangRequest.LineItem> items = new ArrayList<>();
        if (!"KHONG_CO".equalsIgnoreCase(sku) && quantity > 0) {
            items.add(new DatHangRequest.LineItem(sku, quantity));
        }

        var request = new DatHangRequest("an", role, verified, items, shippingData, voucherCode, paymentMethod, giftSku);
        useCase().execute(request);
        this.viewModel = presenter.getViewModel();

        TestWorld world = TestWorld.current();
        world.success = viewModel.success();
        world.message = viewModel.message();
        world.processedDate = viewModel.processedDate();
    }

    @Then("T được thông báo trạng thái đơn hàng là {word} với tổng thanh toán là {long}")
    public void verifyOrderStatusAndTotal(String status, long total) {
        assertTrue(viewModel.success());
        assertEquals(status, viewModel.orderStatus());
        assertEquals(total, viewModel.totalAmount());
    }

    @Then("phí vận chuyển áp dụng là {long} và tồn kho khả dụng của sản phẩm {word} còn {int}")
    public void verifyShippingFeeAndStock(long fee, String sku, int remaining) {
        assertEquals(fee, viewModel.shippingFee());
        assertEquals(remaining, inventoryRepository.stockOf(sku));
    }

    @Then("đơn hàng được lưu lại")
    public void verifyOrderSaved() {
        assertNotNull(viewModel.orderId());
        assertTrue(orderRepository.findById(viewModel.orderId()).isPresent());
    }

    @Then("không có đơn hàng nào được tạo và tồn kho khả dụng giữ nguyên")
    public void verifyNoOrderCreated() {
        assertFalse(viewModel.success());
        if (!"KHONG_CO".equalsIgnoreCase(sku)) {
            assertEquals(initialStock, inventoryRepository.stockOf(sku));
        }
    }

    @Then("đơn hàng được gắn cờ {word} với dòng quà tặng có đơn giá {long}")
    public void verifyOrderFlagAndGiftPrice(String flag, long giftPrice) {
        assertTrue(viewModel.success());
        assertEquals(flag, viewModel.orderFlag());
        assertEquals(giftPrice, viewModel.giftLinePrice());
    }

    @Then("tồn kho riêng của quà tặng còn {int} và đơn hàng được lưu lại")
    public void verifyGiftStockAfter(int remainingGiftStock) {
        if (!"KHONG_CO".equalsIgnoreCase(giftSku)) {
            assertEquals(remainingGiftStock, inventoryRepository.stockOf(giftSku));
        }
        verifyOrderSaved();
    }

    /* --- Voucher hết lượt sau lần đặt trước --- */
    @Given("voucher {word} có mức giảm {long} và chỉ còn {int} lượt dùng")
    public void seedVoucherWithUses(String code, long discount, int uses) {
        voucherRepository.save(new Voucher(code, Money.of(discount), Money.ZERO, today.plusMonths(1), uses));
    }

    @When("T đặt hàng với voucher {word} và phương thức thanh toán {word}")
    public void placeOrderWithVoucher(String vCode, String paymentMethod) {
        this.voucherCode = vCode;
        placeOrder(paymentMethod);
    }

    @Then("đơn hàng lần 1 thành công và voucher {word} còn {int} lượt dùng")
    public void order1SuccessAndVoucherRemaining(String vCode, int expectedRemaining) {
        assertTrue(viewModel.success());
        Voucher v = voucherRepository.findByCode(vCode).orElseThrow();
        assertEquals(expectedRemaining, v.remainingUses());
    }

    @When("T tiếp tục đặt đơn thứ 2 với voucher {word}")
    public void placeOrderSecondTime(String vCode) {
        this.voucherCode = vCode;
        placeOrder("COD");
    }
}
