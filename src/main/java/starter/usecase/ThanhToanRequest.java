package starter.usecase;

/** Plain request DTO for payment actions. */
public record ThanhToanRequest(
    String role, String userId, String orderOwner,
    String orderStatus, String paymentMethod, double totalAmount,
    String action, String gatewayResult,
    double gatewayAmount, String signature,
    int minutesSinceCreated, int recordedBefore
) {}

