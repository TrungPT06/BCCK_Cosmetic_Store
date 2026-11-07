package starter.stepdefinitions;

import java.time.LocalDate;

/**
 * Trạng thái dùng chung trong phạm vi kịch bản (Scenario-scoped) giữa các lớp định nghĩa bước.
 * Sử dụng singleton được hỗ trợ bởi ThreadLocal.
 */
public class TestWorld {
    private static final ThreadLocal<TestWorld> INSTANCE = ThreadLocal.withInitial(TestWorld::new);

    public boolean success;
    public String message;
    public String color;
    public LocalDate processedDate;
    public long refundAmount;
    public boolean stockReleased;

    public static TestWorld current() { return INSTANCE.get(); }
    public static void reset() { INSTANCE.set(new TestWorld()); }
}
