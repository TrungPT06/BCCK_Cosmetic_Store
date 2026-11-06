package starter.usecase;

import starter.entity.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trường hợp sử dụng ứng dụng (Application Use Case / Control in B-C-E): đặt hàng mỹ phẩm.
 *
 * Tuân thủ Clean Architecture và B-C-E:
 * - Giao tiếp với bên ngoài qua DatHangInputBoundary và DatHangOutputBoundary.
 * - Truy vấn giá, hạn dùng và voucher từ ProductRepository và VoucherRepository (Single Source of Truth).
 * - Sử dụng Order Aggregate Root và OrderFactory để bảo vệ các bất biến nghiệp vụ.
 */
public final class DatHangUseCase implements DatHangInputBoundary {
    private final OrderRepository orders;
    private final OrderPolicy policy;
    private final ShippingPolicy shippingPolicy;
    private final PricingPolicy pricingPolicy;
    private final ProductClassification productClassification;
    private final DatHangOutputBoundary outputBoundary;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final OrderFactory orderFactory;

    public DatHangUseCase(OrderRepository orders, OrderPolicy policy,
                          ShippingPolicy shippingPolicy, PricingPolicy pricingPolicy,
                          ProductClassification productClassification,
                          DatHangOutputBoundary outputBoundary,
                          ProductRepository productRepository,
                          VoucherRepository voucherRepository,
                          OrderFactory orderFactory) {
        this.orders = orders;
        this.policy = policy;
        this.shippingPolicy = shippingPolicy;
        this.pricingPolicy = pricingPolicy;
        this.productClassification = productClassification;
        this.outputBoundary = outputBoundary;
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.orderFactory = orderFactory != null ? orderFactory : new OrderFactory();
    }

    public DatHangUseCase(OrderRepository orders, OrderPolicy policy,
                          ShippingPolicy shippingPolicy, PricingPolicy pricingPolicy,
                          ProductClassification productClassification,
                          DatHangOutputBoundary outputBoundary) {
        this(orders, policy, shippingPolicy, pricingPolicy, productClassification, outputBoundary, null, null, null);
    }

    public DatHangUseCase(OrderRepository orders, OrderPolicy policy,
                          ShippingPolicy shippingPolicy, PricingPolicy pricingPolicy,
                          ProductClassification productClassification) {
        this(orders, policy, shippingPolicy, pricingPolicy, productClassification, null, null, null, null);
    }

    @Override
    public DatHangResponse execute(DatHangRequest r) {
        // Single Source of Truth: lấy tồn kho từ Repository
        int currentStock = orders.stockOf(r.sku());
        int currentGiftStock = hasGift(r.giftSku()) ? orders.stockOf(r.giftSku()) : 0;

        // Lấy thông tin sản phẩm từ ProductRepository nếu có, nếu chưa thì fallback từ request
        double unitPrice = r.unitPrice();
        var expiryDate = r.expiryDate();
        if (productRepository != null && r.sku() != null) {
            var productOpt = productRepository.findBySku(r.sku());
            if (productOpt.isPresent()) {
                unitPrice = productOpt.get().unitPrice().amount();
                expiryDate = productOpt.get().expiryDate();
            }
        }

        // Lấy thông tin Voucher từ VoucherRepository nếu có
        Voucher voucher = null;
        if (voucherRepository != null && r.voucher() != null) {
            voucher = voucherRepository.findByCode(r.voucher()).orElse(null);
        }
        if (voucher == null) {
            voucher = Voucher.from(r.voucher(), r.discount(), r.today());
        }

        var draft = new OrderPolicy.OrderDraft(r.role(), r.verified(), r.sku(), r.quantity(), unitPrice,
                expiryDate, currentStock, r.shippingInfo(), r.voucher(), voucher.discountAmount().amount(),
                r.paymentMethod(), r.today(), r.shippingInfoObj(), voucher);

        String error = policy.validate(draft);
        if (error != null) {
            return presentResponse(DatHangResponse.failure(error));
        }

        double merchandise = pricingPolicy.calculateMerchandiseTotal(draft.subtotal(), voucher.discountAmount().amount());
        double shipping = shippingPolicy.calculateShippingFee(draft.subtotal());
        String status = policy.determineInitialStatus(r.paymentMethod());
        String flag = productClassification.determineFlag(r.sku());

        List<Order.OrderLine> lines = new ArrayList<>();
        lines.add(new Order.OrderLine(r.sku(), r.quantity(), unitPrice, false));

        boolean addGift = hasGift(r.giftSku()) && currentGiftStock > 0;
        if (addGift) {
            lines.add(new Order.OrderLine(r.giftSku(), 1, 0.0, true));
        }

        Order order = new Order(UUID.randomUUID().toString(), status, merchandise + shipping, shipping, flag, lines);
        if (!orders.saveAndReserve(order, r.sku(), r.quantity(), addGift ? r.giftSku() : null)) {
            return presentResponse(DatHangResponse.failure("KHONG_THE_LUU_DON_HANG"));
        }

        DatHangResponse successResponse = new DatHangResponse(true, status, status, order.totalAmount(), shipping,
                orders.stockOf(r.sku()), true, flag, 0.0, addGift ? orders.stockOf(r.giftSku()) : currentGiftStock);
        return presentResponse(successResponse);
    }

    private DatHangResponse presentResponse(DatHangResponse response) {
        if (outputBoundary != null) {
            outputBoundary.present(response);
        }
        return response;
    }

    private boolean hasGift(String giftSku) {
        return giftSku != null && !"KHONG_CO".equals(giftSku);
    }
}
