package starter.usecase;

/** DTO yêu cầu thuần túy cho các thao tác thanh toán. */
public record ThanhToanRequest(
    String role, String userId, String orderOwner,
    String orderStatus, String paymentMethod, double totalAmount,
    String action, String gatewayResult,
    double gatewayAmount, String signature,
    int minutesSinceCreated, int recordedBefore
) {}

