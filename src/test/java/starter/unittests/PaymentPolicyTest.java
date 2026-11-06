package starter.unittests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.entity.PaymentPolicy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đặc tả kiểm thử đơn vị cho PaymentPolicy (Enterprise Business Rules)")
class PaymentPolicyTest {
    private final PaymentPolicy policy = new PaymentPolicy();

    private PaymentPolicy.PaymentContext ctx(String role, String userId, String owner,
            String status, String method, String action, String gateway,
            double gwAmount, String sig, int minutes, int recorded) {
        return new PaymentPolicy.PaymentContext(role, userId, owner, status, method, action, gateway,
                gwAmount, sig, 700_000, minutes, recorded);
    }

    @Test @DisplayName("Guest không được thanh toán")
    void rejectsGuest() { assertEquals("YEU_CAU_DANG_NHAP_HOAC_DANG_KY",
        policy.validate(ctx("guest","an","an","CHO_THANH_TOAN","VNPAY","THANH_TOAN_ONLINE",null,0,null,0,0))); }

    @Test @DisplayName("Không thanh toán đơn của người khác")
    void rejectsWrongOwner() { assertEquals("DON_KHONG_THUOC_VE_NGUOI_DUNG",
        policy.validate(ctx("customer","an","binh","CHO_THANH_TOAN","VNPAY","THANH_TOAN_ONLINE",null,0,null,0,0))); }

    @Test @DisplayName("Không thanh toán đơn không ở trạng thái chờ thanh toán")
    void rejectsWrongStatus() { assertEquals("DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN",
        policy.validate(ctx("customer","an","an","DA_XAC_NHAN","VNPAY","THANH_TOAN_ONLINE",null,0,null,0,0))); }

    @Test @DisplayName("Chữ ký callback không hợp lệ")
    void rejectsInvalidSignature() { assertEquals("CHU_KY_CALLBACK_KHONG_HOP_LE",
        policy.validate(ctx("customer","an","an","CHO_THANH_TOAN","VNPAY","THANH_TOAN_ONLINE","THANH_CONG",700_000,"KHONG_HOP_LE",0,0))); }

    @Test @DisplayName("Số tiền không khớp với đơn")
    void rejectsAmountMismatch() { assertEquals("SO_TIEN_KHONG_KHOP_VOI_DON",
        policy.validate(ctx("customer","an","an","CHO_THANH_TOAN","VNPAY","THANH_TOAN_ONLINE","THANH_CONG",500_000,"HOP_LE",0,0))); }

    @Test @DisplayName("Customer không được xác nhận thu tiền COD")
    void rejectsCustomerCod() { assertEquals("KHONG_CO_QUYEN_QUAN_TRI",
        policy.validate(ctx("customer","an","an","DA_GIAO","COD","XAC_NHAN_THU_TIEN_COD",null,0,null,0,0))); }

    @Test @DisplayName("COD chưa giao không thể xác nhận thu tiền")
    void rejectsCodNotDelivered() { assertEquals("DON_COD_CHUA_GIAO_CHUA_THE_XAC_NHAN_THU_TIEN",
        policy.validate(ctx("admin","admin","an","DANG_GIAO","COD","XAC_NHAN_THU_TIEN_COD",null,0,null,0,0))); }

    @Test @DisplayName("Thanh toán hợp lệ vượt qua kiểm tra")
    void validPaymentPasses() { assertNull(
        policy.validate(ctx("customer","an","an","CHO_THANH_TOAN","VNPAY","THANH_TOAN_ONLINE","THANH_CONG",700_000,"HOP_LE",5,0))); }

    @Test @DisplayName("Callback lặp (idempotent) được chấp nhận")
    void duplicateCallbackAccepted() { assertNull(
        policy.validate(ctx("customer","an","an","DA_XAC_NHAN","VNPAY","THANH_TOAN_ONLINE","THANH_CONG",700_000,"HOP_LE",10,1))); }

    @Test @DisplayName("Phát hiện timeout thanh toán")
    void detectsTimeout() { assertTrue(policy.isPaymentTimeout(31)); assertFalse(policy.isPaymentTimeout(30)); }

    @Test @DisplayName("Phát hiện callback lặp")
    void detectsDuplicate() { assertTrue(policy.isDuplicateCallback(1)); assertFalse(policy.isDuplicateCallback(0)); }
}

