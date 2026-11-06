package starter.usecase;

import java.time.LocalDate;

/** Plain request DTO for inventory management actions. */
public record KiemTraTonKhoRequest(
    String role, String action, int requestQty,
    /* batch management */
    String batchId, int changeQty, String reasonInput,
    LocalDate batchExpiry, LocalDate today
) {}

