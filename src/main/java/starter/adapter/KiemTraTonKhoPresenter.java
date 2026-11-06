package starter.adapter;

import starter.usecase.KiemTraTonKhoResponse;

/** Presenter for inventory management. */
public final class KiemTraTonKhoPresenter {
    public KiemTraTonKhoViewModel present(KiemTraTonKhoResponse r) {
        String color = r.success() ? determineColor(r) : "red";
        return new KiemTraTonKhoViewModel(r.message(), r.availableQty(), color);
    }

    private String determineColor(KiemTraTonKhoResponse r) {
        if ("HET_HANG".equals(r.message()) || "DA_NHA_GIU_CHO".equals(r.message())) return "yellow";
        if ("DIEU_CHINH_SAU_KIEM_KE".equals(r.message())) return "yellow";
        if (r.warningStatus() != null) {
            return switch (r.warningStatus()) {
                case "CANH_BAO_DA_HET_HAN" -> "red";
                case "CANH_BAO_SAP_HET_HAN" -> "yellow";
                default -> "green";
            };
        }
        return "green";
    }

    public String determineAdminColor(KiemTraTonKhoResponse r) {
        if (!r.success()) return "red";
        if ("DIEU_CHINH_SAU_KIEM_KE".equals(r.message()) || r.batchQtyAfter() < r.availableQty()) return "yellow";
        return "green";
    }

    public record KiemTraTonKhoViewModel(String message, int availableQty, String color) {}
}

