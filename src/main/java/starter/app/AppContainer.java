package starter.app;

import starter.dathang.adapter.DatHangPresenter;
import starter.dathang.usecase.*;
import starter.domain.OrderFactory;
import starter.domain.OrderPolicy;
import starter.domain.strategy.PaymentStrategyFactory;
import starter.domain.strategy.StandardShippingFeeStrategy;
import starter.infrastructure.*;
import starter.thanhtoan.adapter.ThanhToanPresenter;
import starter.thanhtoan.usecase.*;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Composition Root (AppContainer):
 * Nơi duy nhất trong hệ thống được phép "new" và đấu nối các thành phần phụ thuộc (IoC / Dependency Injection).
 */
public final class AppContainer {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    private final DatHangPresenter datHangPresenter;
    private final ThanhToanPresenter thanhToanPresenter;

    private final DatHangUseCase datHangUseCase;
    private final ThanhToanOnlineUseCase thanhToanOnlineUseCase;
    private final XacNhanThuTienCodUseCase xacNhanThuTienCodUseCase;
    private final HuyDonUseCase huyDonUseCase;

    public AppContainer(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        VoucherRepository voucherRepository,
                        InventoryRepository inventoryRepository,
                        PaymentRepository paymentRepository,
                        PaymentGateway paymentGateway,
                        Clock clock) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;

        this.datHangPresenter = new DatHangPresenter();
        this.thanhToanPresenter = new ThanhToanPresenter();

        PaymentStrategyFactory paymentStrategyFactory = new PaymentStrategyFactory();
        OrderPolicy orderPolicy = new OrderPolicy(paymentStrategyFactory);
        OrderFactory orderFactory = new OrderFactory(paymentStrategyFactory);
        StandardShippingFeeStrategy shippingFeeStrategy = new StandardShippingFeeStrategy();

        this.datHangUseCase = new DatHangUseCase(
                orderRepository, productRepository, voucherRepository, inventoryRepository,
                orderPolicy, orderFactory, shippingFeeStrategy, datHangPresenter, clock
        );

        this.thanhToanOnlineUseCase = new ThanhToanOnlineUseCase(
                orderRepository, paymentRepository, paymentGateway, thanhToanPresenter, clock
        );

        this.xacNhanThuTienCodUseCase = new XacNhanThuTienCodUseCase(
                orderRepository, paymentRepository, thanhToanPresenter, clock
        );

        this.huyDonUseCase = new HuyDonUseCase(
                orderRepository, inventoryRepository, paymentRepository, thanhToanPresenter, clock
        );
    }

    public static AppContainer createInMemory() {
        return new AppContainer(
                new InMemoryOrderRepository(),
                new InMemoryProductRepository(),
                new InMemoryVoucherRepository(),
                new InMemoryInventoryRepository(),
                new InMemoryPaymentRepository(),
                new DefaultPaymentGateway(),
                Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"))
        );
    }

    public static AppContainer createProductionWithH2(String jdbcUrl) {
        return new AppContainer(
                new JdbcOrderRepository(jdbcUrl),
                new InMemoryProductRepository(),
                new InMemoryVoucherRepository(),
                new InMemoryInventoryRepository(),
                new InMemoryPaymentRepository(),
                new DefaultPaymentGateway(),
                Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"))
        );
    }

    public OrderRepository orderRepository() { return orderRepository; }
    public ProductRepository productRepository() { return productRepository; }
    public VoucherRepository voucherRepository() { return voucherRepository; }
    public InventoryRepository inventoryRepository() { return inventoryRepository; }
    public PaymentRepository paymentRepository() { return paymentRepository; }
    public PaymentGateway paymentGateway() { return paymentGateway; }
    public Clock clock() { return clock; }

    public DatHangPresenter datHangPresenter() { return datHangPresenter; }
    public ThanhToanPresenter thanhToanPresenter() { return thanhToanPresenter; }

    public DatHangUseCase datHangUseCase() { return datHangUseCase; }
    public ThanhToanOnlineUseCase thanhToanOnlineUseCase() { return thanhToanOnlineUseCase; }
    public XacNhanThuTienCodUseCase xacNhanThuTienCodUseCase() { return xacNhanThuTienCodUseCase; }
    public HuyDonUseCase huyDonUseCase() { return huyDonUseCase; }
}
