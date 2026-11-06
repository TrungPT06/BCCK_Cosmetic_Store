package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.InventoryPolicy;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho InventoryPolicy (Enterprise Business Rules)")
class InventoryPolicyTest {
    private final InventoryPolicy policy = new InventoryPolicy();
    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @Test @DisplayName("Tính tồn khả dụng = tồn thực tế lô hợp lệ - giữ chỗ")
    void calculatesAvailableStock() {
        var batches = List.of(
            new InventoryPolicy.Batch("L001", 5, today.plusMonths(10)),
            new InventoryPolicy.Batch("L002", 8, today.plusMonths(13)));
        assertEquals(10, policy.calculateAvailableStock(batches, 3, today));
    }

    @Test @DisplayName("Lô hết hạn bị loại khỏi tồn khả dụng")
    void excludesExpiredBatches() {
        var batches = List.of(
            new InventoryPolicy.Batch("L001", 5, today.minusDays(30)),
            new InventoryPolicy.Batch("L002", 8, today.plusMonths(13)));
        assertEquals(5, policy.calculateAvailableStock(batches, 3, today));
    }

    @Test @DisplayName("Lô cận hạn dưới 6 tháng bị loại")
    void excludesNearExpiryBatches() {
        var batches = List.of(
            new InventoryPolicy.Batch("L001", 5, today.plusMonths(2)),
            new InventoryPolicy.Batch("L002", 8, today.plusMonths(13)));
        assertEquals(5, policy.calculateAvailableStock(batches, 3, today));
    }

    @Test @DisplayName("Chọn lô theo FEFO: hết hạn sớm nhất trước")
    void picksFEFOOrder() {
        var batches = List.of(
            new InventoryPolicy.Batch("L001", 5, today.plusMonths(10)),
            new InventoryPolicy.Batch("L002", 8, today.plusMonths(13)));
        assertEquals("L001:4", policy.pickBatchesFEFO(batches, 4, today));
        assertEquals("L001:5+L002:2", policy.pickBatchesFEFO(batches, 7, today));
    }

    @Test @DisplayName("Từ chối số lượng vượt tồn khả dụng")
    void rejectsExceedingQuantity() {
        assertEquals("VUOT_QUA_TON_KHO_KHA_DUNG", policy.validateCheckQuantity(11, 10));
    }

    @Test @DisplayName("Từ chối số lượng bằng 0")
    void rejectsZeroQuantity() {
        assertEquals("SO_LUONG_PHAI_LA_SO_NGUYEN_DUONG", policy.validateCheckQuantity(0, 10));
    }

    @Test @DisplayName("Từ chối số lượng âm")
    void rejectsNegativeQuantity() {
        assertEquals("SO_LUONG_PHAI_LA_SO_NGUYEN_KHONG_AM", policy.validateCheckQuantity(-1, 10));
    }

    @Test @DisplayName("Customer không được nhập kho")
    void rejectsCustomerAdmin() {
        assertEquals("KHONG_CO_QUYEN_QUAN_TRI", policy.validateAdminAction("customer", "NHAP_KHO", 0, 20, today.plusMonths(12), today));
    }

    @Test @DisplayName("Không nhập lô đã hết hạn")
    void rejectsExpiredImport() {
        assertEquals("KHONG_NHAP_LO_DA_HET_HAN_SU_DUNG", policy.validateAdminAction("admin", "NHAP_KHO", 0, 5, today.minusDays(30), today));
    }

    @Test @DisplayName("Điều chỉnh không được làm âm số lượng lô")
    void rejectsNegativeBatchQty() {
        assertEquals("SO_LUONG_LO_KHONG_DUOC_AM", policy.validateAdminAction("admin", "DIEU_CHINH_SAU_KIEM_KE", 8, -10, today.plusMonths(12), today));
    }

    @Test @DisplayName("Cảnh báo lô sắp hết hạn trong 90 ngày")
    void warnsSoonExpiry() {
        var result = policy.checkBatchWarning(today.plusDays(60), today);
        assertEquals("CANH_BAO_SAP_HET_HAN", result.status()); assertEquals(60, result.daysLeft());
    }

    @Test @DisplayName("Cảnh báo lô đã hết hạn")
    void warnsExpired() {
        var result = policy.checkBatchWarning(today.minusDays(34), today);
        assertEquals("CANH_BAO_DA_HET_HAN", result.status()); assertEquals(-34, result.daysLeft());
    }

    @Test @DisplayName("Không cảnh báo lô còn hạn dài")
    void noWarningLongExpiry() {
        var result = policy.checkBatchWarning(today.plusDays(301), today);
        assertEquals("KHONG_CANH_BAO", result.status());
    }
}

