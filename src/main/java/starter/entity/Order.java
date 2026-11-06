package starter.entity;

import java.util.List;

/** Gốc tổng hợp (Aggregate Root). Tầng hạ tầng (Infrastructure) chỉ nhận kết quả nghiệp vụ bất biến này. */
public record Order(String id, String status, double totalAmount, double shippingFee,
                    String flag, List<OrderLine> lines) {
    public record OrderLine(String sku, int quantity, double unitPrice, boolean gift) { }
}
