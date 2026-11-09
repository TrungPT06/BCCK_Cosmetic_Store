package starter.dathang.usecase;

import starter.shared.port.*;
import starter.domain.*;
import starter.domain.strategy.ShippingFeeStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Use Case: Đặt hàng mỹ phẩm.
 * - Tuân thủ Clean Architecture: execute() trả void, gửi kết quả qua DatHangOutputBoundary.
 * - Single Source of Truth: giá và hạn dùng truy vấn từ ProductRepository, tồn kho từ InventoryRepository.
 * - Clock được inject cho ngày hiện tại.
 */
public final class DatHangUseCase implements DatHangInputBoundary {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderPolicy orderPolicy;
    private final OrderFactory orderFactory;
    private final ShippingFeeStrategy shippingFeeStrategy;
    private final DatHangOutputBoundary outputBoundary;
    private final Clock clock;

    public DatHangUseCase(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          VoucherRepository voucherRepository,
                          InventoryRepository inventoryRepository,
                          OrderPolicy orderPolicy,
                          OrderFactory orderFactory,
                          ShippingFeeStrategy shippingFeeStrategy,
                          DatHangOutputBoundary outputBoundary,
                          Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository không được null");
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository không được null");
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "voucherRepository không được null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository không được null");
        this.orderPolicy = Objects.requireNonNull(orderPolicy, "orderPolicy không được null");
        this.orderFactory = Objects.requireNonNull(orderFactory, "orderFactory không được null");
        this.shippingFeeStrategy = Objects.requireNonNull(shippingFeeStrategy, "shippingFeeStrategy không được null");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary không được null");
        this.clock = Objects.requireNonNull(clock, "clock không được null");
    }

    @Override
    public void execute(DatHangRequest request) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        List<DatHangRequest.LineItem> requestItems = request.items() != null ? request.items() : List.of();

        // 1. Nạp danh sách sản phẩm và thẩm định từng dòng
        List<OrderPolicy.ItemValidation> itemValidations = new ArrayList<>();
        Money merchandiseTotal = Money.ZERO;
        boolean hasRestrictedProduct = false;

        {
            for (DatHangRequest.LineItem item : requestItems) {
                if (item == null || item.sku() == null || "KHONG_CO".equalsIgnoreCase(item.sku()) || item.quantity() <= 0) {
                    continue;
                }
                var productOpt = productRepository.findBySku(item.sku());
                if (productOpt.isEmpty()) {
                    outputBoundary.present(DatHangResponse.failure("SAN_PHAM_KHONG_TON_TAI", today));
                    return;
                }
                Product product = productOpt.get();
                if (product.isRestricted()) {
                    hasRestrictedProduct = true;
                }
                int stock = inventoryRepository.stockOf(item.sku());
                itemValidations.add(new OrderPolicy.ItemValidation(product, item.quantity(), stock));
                merchandiseTotal = merchandiseTotal.plus(Money.of(product.unitPrice().amount() * item.quantity()));
            }
        }

        // 2. Nạp Voucher từ VoucherRepository
        Voucher voucher = Voucher.noVoucher();
        if (request.voucherCode() != null && !"KHONG_DUNG".equalsIgnoreCase(request.voucherCode())) {
            var voucherOpt = voucherRepository.findByCode(request.voucherCode());
            if (voucherOpt.isEmpty()) {
                outputBoundary.present(DatHangResponse.failure("VOUCHER_KHONG_TON_TAI", today));
                return;
            }
            voucher = voucherOpt.get();
        }

        // 3. Chuẩn bị ShippingInfo
        ShippingInfo shippingInfo = null;
        if (request.shippingData() != null) {
            shippingInfo = new ShippingInfo(
                    request.shippingData().recipientName(),
                    request.shippingData().phone(),
                    request.shippingData().address(),
                    request.shippingData().city()
            );
        }

        // 4. Thẩm định qua OrderPolicy
        var validationCtx = new OrderPolicy.OrderValidationContext(
                request.role(),
                request.verified(),
                itemValidations,
                shippingInfo,
                voucher,
                merchandiseTotal,
                request.paymentMethod(),
                today
        );

        String error = orderPolicy.validate(validationCtx);
        if (error != null) {
            outputBoundary.present(DatHangResponse.failure(error, today));
            return;
        }

        // 5. Tính toán tài chính
        Money discount = voucher.calculateDiscount(merchandiseTotal);
        Money merchandiseAfterDiscount = merchandiseTotal.minus(discount);
        Money shippingFee = shippingFeeStrategy.calculateShippingFee(merchandiseAfterDiscount);

        // 6. Xây dựng các dòng đơn hàng (OrderLine)
        List<Order.OrderLine> orderLines = new ArrayList<>();
        for (OrderPolicy.ItemValidation item : itemValidations) {
            orderLines.add(new Order.OrderLine(item.product().sku(), item.quantity(), item.product().unitPrice(), false));
        }

        // Xử lý quà tặng kèm (gift promotion)
        boolean hasGift = request.giftSku() != null && !"KHONG_CO".equalsIgnoreCase(request.giftSku());
        int giftStock = hasGift ? inventoryRepository.stockOf(request.giftSku()) : 0;
        boolean addGift = hasGift && giftStock > 0;
        if (addGift) {
            orderLines.add(new Order.OrderLine(Sku.of(request.giftSku()), 1, Money.ZERO, true));
        }

        // 7. Tạo Order qua OrderFactory
        String orderId = UUID.randomUUID().toString();
        Order order = orderFactory.createOrder(orderId, request.customerId(), now, orderLines,
                merchandiseTotal, discount, shippingFee, request.paymentMethod(), hasRestrictedProduct);

        // 8. Giữ chỗ tồn kho (Inventory reserve)
        for (DatHangRequest.LineItem item : requestItems) {
            if (item != null && item.sku() != null && !"KHONG_CO".equalsIgnoreCase(item.sku()) && item.quantity() > 0) {
                inventoryRepository.reserve(item.sku(), item.quantity());
            }
        }
        if (addGift) {
            inventoryRepository.reserve(request.giftSku(), 1);
        }

        // 9. Lưu đơn hàng
        orderRepository.save(order);

        // 10. Giảm lượt voucher & lưu lại
        if (!voucher.isNoVoucher()) {
            voucher.decrementUsage();
            voucherRepository.save(voucher);
        }

        // 11. Chuẩn bị kết quả xuất ra Presenter
        int firstItemRemainingStock = 0;
        if (!requestItems.isEmpty()) {
            firstItemRemainingStock = inventoryRepository.stockOf(requestItems.get(0).sku());
        }
        int giftStockAfter = hasGift ? inventoryRepository.stockOf(request.giftSku()) : 0;

        DatHangResponse response = DatHangResponse.success(
                order.id(),
                order.status(),
                order.total(),
                order.shippingFee(),
                firstItemRemainingStock,
                order.flag(),
                Money.ZERO,
                giftStockAfter,
                today
        );

        outputBoundary.present(response);
    }
}
