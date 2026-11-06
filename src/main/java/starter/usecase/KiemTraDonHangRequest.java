package starter.usecase;

/**
 * Plain request DTO for order management actions.
 * Covers: view, cancel, return, status transition, search, edit line.
 */
public record KiemTraDonHangRequest(
    String role, String userId, String orderOwner,
    String currentStatus, String paymentStatus, double totalAmount,
    String action, String targetStatus,
    String reasonInput, String evidence,
    int daysSinceDelivered, int returnWindowDays, String sealStatus,
    /* search fields */
    String filterOrderId, String filterCustomer, String filterStatus, String filterDateRange, int pageSize
) {
    /** Convenience factory for non-search actions */
    public static KiemTraDonHangRequest action(String role, String userId, String orderOwner,
            String currentStatus, String paymentStatus, double totalAmount,
            String action, String targetStatus, String reasonInput, String evidence,
            int daysSinceDelivered, int returnWindowDays, String sealStatus) {
        return new KiemTraDonHangRequest(role, userId, orderOwner, currentStatus, paymentStatus, totalAmount,
                action, targetStatus, reasonInput, evidence, daysSinceDelivered, returnWindowDays, sealStatus,
                null, null, null, null, 0);
    }
}

