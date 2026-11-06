package starter.usecase;

import starter.entity.InventoryPolicy;
import java.time.LocalDate;
import java.util.List;

/**
 * Output port for inventory management. Implementation in infrastructure layer.
 */
public interface InventoryRepository {
    List<InventoryPolicy.Batch> getBatches();
    int getReserved();
    void reserve(int qty);
    void release(int qty);
    void updateBatchQty(String batchId, int newQty);
    int getBatchQty(String batchId);
    void addBatch(String batchId, int qty, LocalDate expiry);
    void addLog(String who, String action, String batchId, int oldQty, int newQty, String reason);
}

