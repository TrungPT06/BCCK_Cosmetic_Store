package starter.thanhtoan.adapter;

import starter.domain.Money;
import starter.thanhtoan.usecase.ThanhToanInputBoundary;
import starter.thanhtoan.usecase.ThanhToanRequest;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface Adapter: ThanhToanController (lát cắt dọc Thanh toán).
 * Nhận yêu cầu HTTP dạng JSON, chuyển thành ThanhToanRequest, chọn use case theo "action",
 * rồi trả HTTP Response dựng từ ViewModel của Presenter.
 *
 * action: THANH_TOAN_ONLINE | THANH_TOAN_LAI_DOI_PHUONG_THUC -> use case thanh toán online
 *         XAC_NHAN_THU_TIEN_COD                               -> use case xác nhận thu tiền COD
 *         HUY_DON | HUY_QUA_HAN_THANH_TOAN                    -> use case hủy đơn
 */
public final class ThanhToanController {
    private final ThanhToanInputBoundary onlineUseCase;
    private final ThanhToanInputBoundary codUseCase;
    private final ThanhToanInputBoundary huyDonUseCase;
    private final ThanhToanPresenter presenter;

    public ThanhToanController(ThanhToanInputBoundary onlineUseCase,
                               ThanhToanInputBoundary codUseCase,
                               ThanhToanInputBoundary huyDonUseCase,
                               ThanhToanPresenter presenter) {
        this.onlineUseCase = Objects.requireNonNull(onlineUseCase, "onlineUseCase không được null");
        this.codUseCase = Objects.requireNonNull(codUseCase, "codUseCase không được null");
        this.huyDonUseCase = Objects.requireNonNull(huyDonUseCase, "huyDonUseCase không được null");
        this.presenter = Objects.requireNonNull(presenter, "presenter không được null");
    }

    public HttpResponse handle(HttpRequest request) {
        if (!"POST".equalsIgnoreCase(request.method())) {
            return new HttpResponse(405, "{\"error\":\"METHOD_NOT_ALLOWED\"}");
        }
        String body = request.body();
        String orderId = str(body, "orderId");
        String userId = str(body, "userId");
        String role = str(body, "role");
        String action = str(body, "action");
        if (orderId == null) return missing("orderId");
        if (userId == null) return missing("userId");
        if (role == null) return missing("role");
        if (action == null) return missing("action");

        ThanhToanInputBoundary target = switch (action) {
            case "THANH_TOAN_ONLINE", "THANH_TOAN_LAI_DOI_PHUONG_THUC" -> onlineUseCase;
            case "XAC_NHAN_THU_TIEN_COD" -> codUseCase;
            case "HUY_DON", "HUY_QUA_HAN_THANH_TOAN" -> huyDonUseCase;
            default -> null;
        };
        if (target == null) {
            return new HttpResponse(400, "{\"success\":false,\"message\":\"ACTION_KHONG_HOP_LE\"}");
        }

        String amountText = str(body, "gatewayAmount");
        Money gatewayAmount = amountText != null && amountText.matches("\\d+") ? Money.of(Long.parseLong(amountText)) : Money.ZERO;
        target.execute(new ThanhToanRequest(orderId, userId, role, action,
                str(body, "gatewayResult"), gatewayAmount, str(body, "signature")));

        ThanhToanPresenter.ThanhToanViewModel vm = presenter.getViewModel();
        if (vm == null) {
            return new HttpResponse(500, "{\"error\":\"NO_VIEW_MODEL\"}");
        }
        String json = String.format(
                "{\"success\":%s,\"orderId\":\"%s\",\"orderStatus\":\"%s\",\"paymentStatus\":\"%s\",\"refundAmount\":%d,\"stockReleased\":%s,\"message\":\"%s\"}",
                vm.success(),
                nz(vm.orderId()),
                nz(vm.orderStatusAfter()),
                nz(vm.paymentStatus()),
                vm.refundAmount(),
                vm.stockReleased(),
                nz(vm.message()));
        return new HttpResponse(vm.success() ? 200 : 400, json);
    }

    private static HttpResponse missing(String field) {
        return new HttpResponse(400, "{\"success\":false,\"message\":\"THIEU_TRUONG_BAT_BUOC_" + field + "\"}");
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    /** Lấy giá trị chuỗi hoặc số của khóa JSON phẳng; trả null nếu không có. */
    private static String str(String json, String key) {
        if (json == null) return null;
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+))").matcher(json);
        if (!m.find()) return null;
        return m.group(1) != null ? m.group(1) : m.group(2);
    }

    public record HttpRequest(String method, String path, String body) {
        public static HttpRequest post(String path, String body) {
            return new HttpRequest("POST", path, body);
        }
    }

    public record HttpResponse(int statusCode, String body) {}
}
