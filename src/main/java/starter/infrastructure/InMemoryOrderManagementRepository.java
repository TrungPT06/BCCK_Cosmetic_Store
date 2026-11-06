package starter.infrastructure;

import starter.usecase.OrderManagementRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class InMemoryOrderManagementRepository implements OrderManagementRepository {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final List<ManagedOrder> orders = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private String currentStatus;
    private boolean stockReleased;

    public void setCurrentStatus(String status) { this.currentStatus = status; }

    public void addSampleOrders(List<ManagedOrder> samples) { orders.addAll(samples); }

    @Override public boolean updateStatus(String newStatus) { currentStatus = newStatus; return true; }
    @Override public boolean releaseStock() { stockReleased = true; return true; }
    @Override public void addHistoryEntry(String oldStatus, String newStatus) { history.add(oldStatus + "->" + newStatus); }
    @Override public int orderCount() { return orders.size(); }
    public boolean hasHistory() { return !history.isEmpty(); }
    public boolean isStockReleased() { return stockReleased; }

    @Override
    public List<ManagedOrder> search(String fId, String fCust, String fStatus, String dateStart, String dateEnd) {
        return orders.stream().filter(o -> {
            if (fId != null && !o.orderId().equals(fId)) return false;
            if (fCust != null && !o.owner().equals(fCust)) return false;
            if (fStatus != null && !o.status().equals(fStatus)) return false;
            if (dateStart != null && dateEnd != null) {
                LocalDate start = LocalDate.parse(dateStart, DATE);
                LocalDate end = LocalDate.parse(dateEnd, DATE);
                if (o.createdDate().isBefore(start) || o.createdDate().isAfter(end)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }
}

