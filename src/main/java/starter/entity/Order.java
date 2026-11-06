package starter.entity;

import java.util.List;

/** Aggregate root. Infrastructure only receives this immutable business result. */
public record Order(String id, String status, double totalAmount, double shippingFee,
                    String flag, List<OrderLine> lines) {
    public record OrderLine(String sku, int quantity, double unitPrice, boolean gift) { }
}
