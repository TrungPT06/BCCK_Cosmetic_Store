package starter.usecase;

import starter.entity.InventoryPolicy;
import java.util.List;

public final class KiemTraTonKhoUseCase implements KiemTraTonKhoInputBoundary {
    private final InventoryRepository repo;
    private final InventoryPolicy policy;

    public KiemTraTonKhoUseCase(InventoryRepository repo, InventoryPolicy policy) {
        this.repo = repo; this.policy = policy;
    }

    @Override
    public KiemTraTonKhoResponse execute(KiemTraTonKhoRequest r) {
        return switch (r.action()) {
            case "KIEM_TRA_SO_LUONG" -> checkQuantity(r);
            case "GIU_CHO" -> reserve(r);
            case "NHA_GIU_CHO" -> release(r);
            case "XEM_SAN_PHAM" -> viewProduct(r);
            case "NHAP_KHO" -> importBatch(r);
            case "DIEU_CHINH_SAU_KIEM_KE" -> adjustBatch(r);
            case "QUET_CANH_BAO_LO_SAP_HET_HAN" -> scanWarning(r);
            default -> KiemTraTonKhoResponse.failure("HANH_DONG_KHONG_HOP_LE", 0);
        };
    }

    private KiemTraTonKhoResponse checkQuantity(KiemTraTonKhoRequest r) {
        List<InventoryPolicy.Batch> batches = repo.getBatches();
        int reserved = repo.getReserved();
        int available = policy.calculateAvailableStock(batches, reserved, r.today());

        if (!policy.hasValidBatches(batches, r.today()) && available <= 0)
            return KiemTraTonKhoResponse.failure("KHONG_CON_LO_DAT_DIEU_KIEN_BAN", 0);

        String error = policy.validateCheckQuantity(r.requestQty(), available);
        if (error != null) return KiemTraTonKhoResponse.failure(error, available);

        String picked = policy.pickBatchesFEFO(batches, r.requestQty(), r.today());
        return KiemTraTonKhoResponse.checkSuccess("DU_TON", available, picked);
    }

    private KiemTraTonKhoResponse reserve(KiemTraTonKhoRequest r) {
        List<InventoryPolicy.Batch> batches = repo.getBatches();
        int reserved = repo.getReserved();
        int available = policy.calculateAvailableStock(batches, reserved, r.today());

        String error = policy.validateCheckQuantity(r.requestQty(), available);
        if (error != null) return KiemTraTonKhoResponse.failure(error, available);

        repo.reserve(r.requestQty());
        int newAvailable = policy.calculateAvailableStock(batches, repo.getReserved(), r.today());
        String picked = policy.pickBatchesFEFO(batches, r.requestQty(), r.today());
        return KiemTraTonKhoResponse.reserveSuccess("DA_GIU_CHO", newAvailable, picked);
    }

    private KiemTraTonKhoResponse release(KiemTraTonKhoRequest r) {
        repo.release(r.requestQty());
        List<InventoryPolicy.Batch> batches = repo.getBatches();
        int newAvailable = policy.calculateAvailableStock(batches, repo.getReserved(), r.today());
        return KiemTraTonKhoResponse.reserveSuccess("DA_NHA_GIU_CHO", newAvailable, "KHONG_AP_DUNG");
    }

    private KiemTraTonKhoResponse viewProduct(KiemTraTonKhoRequest r) {
        List<InventoryPolicy.Batch> batches = repo.getBatches();
        int reserved = repo.getReserved();
        int available = policy.calculateAvailableStock(batches, reserved, r.today());
        String result = available > 0 ? "CON_HANG" : "HET_HANG";
        return KiemTraTonKhoResponse.checkSuccess(result, available, "KHONG_AP_DUNG");
    }

    private KiemTraTonKhoResponse importBatch(KiemTraTonKhoRequest r) {
        String error = policy.validateAdminAction(r.role(), r.action(), 0, r.changeQty(), r.batchExpiry(), r.today());
        if (error != null) return KiemTraTonKhoResponse.adminFailure(error);
        int oldQty = repo.getBatchQty(r.batchId());
        int newQty = oldQty + r.changeQty();
        repo.addBatch(r.batchId(), newQty, r.batchExpiry());
        repo.addLog(r.role(), r.action(), r.batchId(), oldQty, newQty, r.reasonInput());
        return KiemTraTonKhoResponse.adminSuccess(r.action(), newQty);
    }

    private KiemTraTonKhoResponse adjustBatch(KiemTraTonKhoRequest r) {
        int oldQty = repo.getBatchQty(r.batchId());
        String error = policy.validateAdminAction(r.role(), r.action(), oldQty, r.changeQty(), r.batchExpiry(), r.today());
        if (error != null) return KiemTraTonKhoResponse.adminFailure(error);
        int newQty = oldQty + r.changeQty();
        repo.updateBatchQty(r.batchId(), newQty);
        repo.addLog(r.role(), r.action(), r.batchId(), oldQty, newQty, r.reasonInput());
        return KiemTraTonKhoResponse.adminSuccess(r.action(), newQty);
    }

    private KiemTraTonKhoResponse scanWarning(KiemTraTonKhoRequest r) {
        var warning = policy.checkBatchWarning(r.batchExpiry(), r.today());
        return KiemTraTonKhoResponse.warningResult(warning.status(), warning.daysLeft());
    }
}

