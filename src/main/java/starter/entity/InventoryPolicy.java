package starter.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Enterprise policy for inventory management by batch.
 *
 * Single Responsibility: quản lý tồn kho theo lô (batch), tính tồn khả dụng,
 * chọn lô xuất theo FEFO, cảnh báo hạn sử dụng.
 * Thay đổi khi: quy tắc xuất kho, ngưỡng hạn sử dụng, ngưỡng cảnh báo thay đổi.
 */
public final class InventoryPolicy {
    private static final long MIN_EXPIRY_MONTHS = 6;
    private static final int WARNING_THRESHOLD_DAYS = 90;

    public record Batch(String batchId, int quantity, LocalDate expiryDate) {}
    public record WarningResult(String status, int daysLeft) {}

    public boolean isValidBatch(Batch batch, LocalDate today) {
        if (batch.expiryDate() == null) return true;
        if (!batch.expiryDate().isAfter(today)) return false;
        return !batch.expiryDate().isBefore(today.plusMonths(MIN_EXPIRY_MONTHS));
    }

    public int calculateAvailableStock(List<Batch> batches, int reserved, LocalDate today) {
        int physicalValid = batches.stream().filter(b -> isValidBatch(b, today)).mapToInt(Batch::quantity).sum();
        return physicalValid - reserved;
    }

    public String pickBatchesFEFO(List<Batch> batches, int requestQty, LocalDate today) {
        List<Batch> valid = batches.stream().filter(b -> isValidBatch(b, today))
                .sorted(Comparator.comparing(Batch::expiryDate)).toList();
        StringBuilder sb = new StringBuilder();
        int remaining = requestQty;
        for (Batch b : valid) {
            if (remaining <= 0) break;
            int pick = Math.min(remaining, b.quantity());
            if (pick > 0) {
                if (sb.length() > 0) sb.append("+");
                sb.append(b.batchId()).append(":").append(pick);
                remaining -= pick;
            }
        }
        return sb.length() > 0 ? sb.toString() : "KHONG_AP_DUNG";
    }

    public String validateCheckQuantity(int requestQty, int available) {
        if (requestQty < 0) return "SO_LUONG_PHAI_LA_SO_NGUYEN_KHONG_AM";
        if (requestQty == 0) return "SO_LUONG_PHAI_LA_SO_NGUYEN_DUONG";
        if (requestQty > available) return "VUOT_QUA_TON_KHO_KHA_DUNG";
        return null;
    }

    public boolean hasValidBatches(List<Batch> batches, LocalDate today) {
        return batches.stream().anyMatch(b -> isValidBatch(b, today));
    }

    public String validateAdminAction(String role, String action, int qtyBefore, int changeQty,
                                       LocalDate expiryDate, LocalDate today) {
        if (!"admin".equalsIgnoreCase(role)) return "KHONG_CO_QUYEN_QUAN_TRI";
        if ("NHAP_KHO".equals(action) && expiryDate != null && !expiryDate.isAfter(today))
            return "KHONG_NHAP_LO_DA_HET_HAN_SU_DUNG";
        if ("DIEU_CHINH_SAU_KIEM_KE".equals(action) && (qtyBefore + changeQty) < 0)
            return "SO_LUONG_LO_KHONG_DUOC_AM";
        return null;
    }

    public WarningResult checkBatchWarning(LocalDate expiryDate, LocalDate today) {
        int daysLeft = (int) ChronoUnit.DAYS.between(today, expiryDate);
        if (daysLeft < 0) return new WarningResult("CANH_BAO_DA_HET_HAN", daysLeft);
        if (daysLeft <= WARNING_THRESHOLD_DAYS) return new WarningResult("CANH_BAO_SAP_HET_HAN", daysLeft);
        return new WarningResult("KHONG_CANH_BAO", daysLeft);
    }
}

