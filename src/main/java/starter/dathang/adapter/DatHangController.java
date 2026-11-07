package starter.dathang.adapter;

import starter.dathang.usecase.DatHangInputBoundary;
import starter.dathang.usecase.DatHangRequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface Adapter: DatHangController (Tracer Bullet).
 * Controller tiếp nhận yêu cầu dạng chuỗi JSON / HTTP, chuyển đổi thành DatHangRequest,
 * gọi Input Boundary và trả về HTTP Response DTO.
 */
public final class DatHangController {
    private final DatHangInputBoundary datHangUseCase;
    private final DatHangPresenter presenter;

    public DatHangController(DatHangInputBoundary datHangUseCase, DatHangPresenter presenter) {
        this.datHangUseCase = Objects.requireNonNull(datHangUseCase, "datHangUseCase không được null");
        this.presenter = Objects.requireNonNull(presenter, "presenter không được null");
    }

    public HttpResponse handle(HttpRequest request) {
        if (!"POST".equalsIgnoreCase(request.method())) {
            return new HttpResponse(405, "{\"error\":\"METHOD_NOT_ALLOWED\"}");
        }

        DatHangRequest req = parseRequest(request.body());
        datHangUseCase.execute(req);

        DatHangPresenter.DatHangViewModel vm = presenter.getViewModel();
        if (vm == null) {
            return new HttpResponse(500, "{\"error\":\"NO_VIEW_MODEL\"}");
        }

        int statusCode = vm.success() ? 201 : 400;
        String json = String.format(
                "{\"success\":%s,\"orderStatus\":\"%s\",\"totalAmount\":%d,\"shippingFee\":%d,\"orderFlag\":\"%s\",\"message\":\"%s\"}",
                vm.success(),
                vm.orderStatus() != null ? vm.orderStatus() : "",
                vm.totalAmount(),
                vm.shippingFee(),
                vm.orderFlag() != null ? vm.orderFlag() : "",
                vm.message() != null ? vm.message() : ""
        );

        return new HttpResponse(statusCode, json);
    }

    private DatHangRequest parseRequest(String json) {
        String customerId = extractString(json, "customerId", "an");
        String role = extractString(json, "role", "customer");
        boolean verified = extractBoolean(json, "verified", true);
        String voucherCode = extractString(json, "voucherCode", "KHONG_DUNG");
        String paymentMethod = extractString(json, "paymentMethod", "COD");
        String giftSku = extractString(json, "giftSku", "KHONG_CO");

        String recipientName = extractString(json, "recipientName", "Nguyễn Văn An");
        String phone = extractString(json, "phone", "0912345678");
        String address = extractString(json, "address", "123 Đường Mỹ Phẩm");
        String city = extractString(json, "city", "TP. Hồ Chí Minh");

        List<DatHangRequest.LineItem> items = new ArrayList<>();
        Pattern itemPattern = Pattern.compile("\\{\\s*\"sku\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"quantity\"\\s*:\\s*(\\d+)\\s*\\}");
        Matcher m = itemPattern.matcher(json);
        while (m.find()) {
            items.add(new DatHangRequest.LineItem(m.group(1), Integer.parseInt(m.group(2))));
        }
        if (items.isEmpty()) {
            items.add(new DatHangRequest.LineItem("SERUM_VITC_30", 2));
        }

        return new DatHangRequest(
                customerId, role, verified, items,
                new DatHangRequest.ShippingData(recipientName, phone, address, city),
                voucherCode, paymentMethod, giftSku
        );
    }

    private String extractString(String json, String key, String defaultValue) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : defaultValue;
    }

    private boolean extractBoolean(String json, String key, boolean defaultValue) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : defaultValue;
    }

    public record HttpRequest(String method, String path, String body) {
        public static HttpRequest post(String path, String body) {
            return new HttpRequest("POST", path, body);
        }
    }

    public record HttpResponse(int statusCode, String body) {}
}
