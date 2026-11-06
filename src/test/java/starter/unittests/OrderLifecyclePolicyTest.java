package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.OrderLifecyclePolicy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho OrderLifecyclePolicy (Enterprise Business Rules)")
class OrderLifecyclePolicyTest {
    private final OrderLifecyclePolicy policy = new OrderLifecyclePolicy();

    private OrderLifecyclePolicy.OrderActionContext ctx(String role, String userId, String owner,
            String status, String payment, String action, String target, String reason, String evidence,
            int days, int window, String seal) {
        return new OrderLifecyclePolicy.OrderActionContext(role, userId, owner, status, payment, 650_000,
                action, target, reason, evidence, days, window, seal);
    }

    @Test @DisplayName("Guest không được phép xem đơn")
    void rejectsGuest() { assertEquals("YEU_CAU_DANG_NHAP_HOAC_DANG_KY",
        policy.validateAction(ctx("guest","an","an","DA_XAC_NHAN","DA_THANH_TOAN","XEM_DON_HANG",null,null,null,0,7,null))); }

    @Test @DisplayName("Customer không xem được đơn của người khác")
    void rejectsWrongOwner() { assertEquals("DON_KHONG_THUOC_VE_NGUOI_DUNG",
        policy.validateAction(ctx("customer","an","binh","DA_XAC_NHAN","DA_THANH_TOAN","XEM_DON_HANG",null,null,null,0,7,null))); }

    @Test @DisplayName("Không hủy được đơn đang giao")
    void rejectsCancelShipping() { assertEquals("KHONG_THE_HUY_DON_DA_CHUYEN_SANG_DANG_GIAO",
        policy.validateAction(ctx("customer","an","an","DANG_GIAO","DA_THANH_TOAN","HUY_DON",null,null,null,0,7,null))); }

    @Test @DisplayName("Customer không có quyền chuyển trạng thái")
    void rejectsCustomerTransition() { assertEquals("KHONG_CO_QUYEN_QUAN_TRI",
        policy.validateAction(ctx("customer","an","an","DA_XAC_NHAN","DA_THANH_TOAN","CHUYEN_TRANG_THAI","DANG_CHUAN_BI",null,null,0,7,null))); }

    @Test @DisplayName("Admin chuyển trạng thái hợp lệ theo thứ tự")
    void validTransition() { assertNull(
        policy.validateAction(ctx("admin","admin","an","DA_XAC_NHAN","DA_THANH_TOAN","CHUYEN_TRANG_THAI","DANG_CHUAN_BI",null,null,0,7,null))); }

    @Test @DisplayName("Không nhảy cóc trạng thái")
    void rejectsSkipTransition() { assertEquals("CHUYEN_TRANG_THAI_KHONG_HOP_LE",
        policy.validateAction(ctx("admin","admin","an","DA_XAC_NHAN","DA_THANH_TOAN","CHUYEN_TRANG_THAI","DANG_GIAO",null,null,0,7,null))); }

    @Test @DisplayName("Quá thời hạn đổi trả 7 ngày")
    void rejectsLateReturn() { assertEquals("QUA_THOI_HAN_DOI_TRA",
        policy.validateAction(ctx("customer","an","an","DA_GIAO","DA_THANH_TOAN","YEU_CAU_TRA_HANG",null,"GIAO_SAI_SAN_PHAM","ANH_MO_HOP",10,7,"CHUA_MO_SEAL"))); }

    @Test @DisplayName("Trả hàng hợp lệ trong hạn")
    void validReturn() { assertNull(
        policy.validateAction(ctx("customer","an","an","DA_GIAO","DA_THANH_TOAN","YEU_CAU_TRA_HANG",null,"GIAO_SAI_SAN_PHAM","ANH_MO_HOP",3,7,"DA_MO_SEAL"))); }

    @Test @DisplayName("Thiếu bằng chứng khi trả hàng")
    void rejectsMissingEvidence() { assertEquals("THIEU_BANG_CHUNG_ANH_VIDEO_MO_HOP",
        policy.validateAction(ctx("customer","an","an","DA_GIAO","DA_THANH_TOAN","YEU_CAU_TRA_HANG",null,"HU_HONG_KHI_VAN_CHUYEN","KHONG_CO",2,7,"CHUA_MO_SEAL"))); }

    @Test @DisplayName("Tính hoàn tiền khi hủy đơn đã thanh toán")
    void refundOnCancel() { assertEquals(650_000, policy.calculateRefund("HUY_DON", "DA_THANH_TOAN", 650_000)); }

    @Test @DisplayName("Không hoàn tiền khi hủy đơn chưa thanh toán")
    void noRefundUnpaid() { assertEquals(0.0, policy.calculateRefund("HUY_DON", "CHUA_THANH_TOAN", 650_000)); }

    @Test @DisplayName("Nhả tồn kho khi hủy đơn")
    void releaseStockOnCancel() { assertTrue(policy.shouldReleaseStock("HUY_DON")); }

    @Test @DisplayName("Không nhả tồn kho khi xem đơn")
    void noReleaseOnView() { assertFalse(policy.shouldReleaseStock("XEM_DON_HANG")); }
}

