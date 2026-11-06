package starter.stepdefinitions;

import java.time.LocalDate;

/**
 * Trạng thái dùng chung trong phạm vi kịch bản (Scenario-scoped) giữa các lớp định nghĩa bước.
 * Sử dụng singleton được hỗ trợ bởi ThreadLocal để SerenityObjectFactory của Serenity
 * (vốn yêu cầu hàm khởi tạo không tham số) vẫn có thể chia sẻ trạng thái qua các lớp bước.
 *
 * Mỗi kịch bản được thiết lập lại qua hook @Before trong CucumberHooks.
 */
public class TestWorld {
    private static final ThreadLocal<TestWorld> INSTANCE = ThreadLocal.withInitial(TestWorld::new);

    public boolean success;
    public String message;
    public String color;
    public LocalDate processedDate;
    public double refundAmount;
    public boolean stockReleased;

    public static TestWorld current() { return INSTANCE.get(); }
    public static void reset() { INSTANCE.set(new TestWorld()); }
}
