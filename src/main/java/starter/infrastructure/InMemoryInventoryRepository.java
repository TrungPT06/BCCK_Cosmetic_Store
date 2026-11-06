package starter.infrastructure;

import starter.entity.InventoryPolicy;
import starter.usecase.InventoryRepository;
import java.time.LocalDate;
import java.util.*;

public final class InMemoryInventoryRepository implements InventoryRepository {
    private final List<InventoryPolicy.Batch> batches = new ArrayList<>();
    private int reserved;
    private final List<String> logs = new ArrayList<>();

    public void setup(List<InventoryPolicy.Batch> initial, int reserved) {
        batches.clear(); batches.addAll(initial); this.reserved = reserved;
    }

    @Override public List<InventoryPolicy.Batch> getBatches() { return List.copyOf(batches); }
    @Override public int getReserved() { return reserved; }
    @Override public void reserve(int qty) { reserved += qty; }
    @Override public void release(int qty) { reserved = Math.max(0, reserved - qty); }

    @Override public void updateBatchQty(String batchId, int newQty) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).batchId().equals(batchId)) {
                batches.set(i, new InventoryPolicy.Batch(batchId, newQty, batches.get(i).expiryDate()));
                return;
            }
        }
    }

    @Override public int getBatchQty(String batchId) {
        return batches.stream().filter(b -> b.batchId().equals(batchId)).mapToInt(InventoryPolicy.Batch::quantity).findFirst().orElse(0);
    }

    @Override public void addBatch(String batchId, int qty, LocalDate expiry) {
        batches.removeIf(b -> b.batchId().equals(batchId));
        batches.add(new InventoryPolicy.Batch(batchId, qty, expiry));
    }

    @Override public void addLog(String who, String action, String batchId, int oldQty, int newQty, String reason) {
        logs.add(String.format("%s|%s|%s|%d->%d|%s", who, action, batchId, oldQty, newQty, reason));
    }

    public boolean hasLogs() { return !logs.isEmpty(); }
}

