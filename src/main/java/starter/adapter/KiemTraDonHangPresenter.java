package starter.adapter;

import starter.usecase.KiemTraDonHangResponse;

/** Presenter for order management — maps response to display-ready ViewModel with color. */
public final class KiemTraDonHangPresenter {
    public KiemTraDonHangViewModel present(KiemTraDonHangResponse r) {
        String color = r.success() ? determineColor(r) : "red";
        return new KiemTraDonHangViewModel(r.message(), r.newStatus(), color);
    }

    private String determineColor(KiemTraDonHangResponse r) {
        if (r.matchedCount() > 0 || "TIM_KIEM_THANH_CONG".equals(r.message())) {
            return r.matchedCount() > 0 ? "green" : "yellow";
        }
        String status = r.newStatus();
        if (status == null) return "green";
        return switch (status) {
            case "DA_HUY", "YEU_CAU_TRA_HANG", "CHO_THANH_TOAN" -> "yellow";
            default -> "green";
        };
    }

    public record KiemTraDonHangViewModel(String message, String newStatus, String color) {}
}

