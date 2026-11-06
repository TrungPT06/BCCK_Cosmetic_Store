package starter.usecase;

import starter.entity.OrderLifecyclePolicy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class KiemTraDonHangUseCase implements KiemTraDonHangInputBoundary {
    private final OrderManagementRepository repo;
    private final OrderLifecyclePolicy policy;

    public KiemTraDonHangUseCase(OrderManagementRepository repo, OrderLifecyclePolicy policy) {
        this.repo = repo; this.policy = policy;
    }

    @Override
    public KiemTraDonHangResponse execute(KiemTraDonHangRequest r) {
        if ("TIM_KIEM_DON_HANG".equals(r.action())) return executeSearch(r);

        var ctx = new OrderLifecyclePolicy.OrderActionContext(r.role(), r.userId(), r.orderOwner(),
                r.currentStatus(), r.paymentStatus(), r.totalAmount(),
                r.action(), r.targetStatus(), r.reasonInput(), r.evidence(),
                r.daysSinceDelivered(), r.returnWindowDays(), r.sealStatus());

        String error = policy.validateAction(ctx);
        if (error != null) return KiemTraDonHangResponse.failure(error);

        if ("XEM_DON_HANG".equals(r.action())) return KiemTraDonHangResponse.viewSuccess(r.currentStatus());

        String newStatus = policy.determineNewStatus(r.action(), r.targetStatus());
        double refund = policy.calculateRefund(r.action(), r.paymentStatus(), r.totalAmount());
        boolean release = policy.shouldReleaseStock(r.action());
        boolean changed = newStatus != null && !newStatus.equals(r.currentStatus());

        if (newStatus != null) repo.updateStatus(newStatus);
        if (release) repo.releaseStock();
        if (changed) repo.addHistoryEntry(r.currentStatus(), newStatus);

        return KiemTraDonHangResponse.actionSuccess(newStatus != null ? newStatus : r.currentStatus(), refund, release, changed);
    }

    private KiemTraDonHangResponse executeSearch(KiemTraDonHangRequest r) {
        if (!"admin".equalsIgnoreCase(r.role())) return KiemTraDonHangResponse.failure("KHONG_CO_QUYEN_QUAN_TRI");

        String dateStart = null, dateEnd = null;
        if (r.filterDateRange() != null && !"KHONG_AP_DUNG".equals(r.filterDateRange())) {
            String[] parts = r.filterDateRange().split("-");
            dateStart = parts[0]; dateEnd = parts[1];
        }
        String fId = "KHONG_AP_DUNG".equals(r.filterOrderId()) ? null : r.filterOrderId();
        String fCust = "KHONG_AP_DUNG".equals(r.filterCustomer()) ? null : r.filterCustomer();
        String fStatus = "KHONG_AP_DUNG".equals(r.filterStatus()) ? null : r.filterStatus();

        var results = repo.search(fId, fCust, fStatus, dateStart, dateEnd);
        int matched = results.size();
        int pages = r.pageSize() > 0 ? (int) Math.ceil((double) matched / r.pageSize()) : 0;
        return KiemTraDonHangResponse.searchSuccess(matched, pages);
    }
}

