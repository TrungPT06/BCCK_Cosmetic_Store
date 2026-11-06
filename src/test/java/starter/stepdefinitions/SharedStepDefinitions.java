package starter.stepdefinitions;

import io.cucumber.java.en.Then;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Các định nghĩa bước (Step Definitions) dùng chung cho các mẫu bước xuất hiện ở nhiều feature.
 * Tránh lỗi DuplicateStepDefinitionException bằng cách định nghĩa mỗi mẫu bước một lần duy nhất.
 */
public class SharedStepDefinitions {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Then("T được thông báo lỗi là {word} với màu red")
    @Then("T được thông báo lỗi là {string} với màu red")
    public void failure(String error) {
        var world = TestWorld.current();
        assertFalse(world.success);
        assertEquals(error, world.message);
        assertEquals("red", world.color);
    }

    @Then("T được thông báo lỗi là {word}")
    @Then("T được thông báo lỗi là {string}")
    public void failureWithoutColor(String error) {
        var world = TestWorld.current();
        assertFalse(world.success);
        assertEquals(error, world.message);
    }

    @Then("đơn hàng được xử lý vào ngày {word}")
    @Then("đơn hàng được xử lý vào ngày {string}")
    public void processedDate(String date) {
        assertEquals(LocalDate.parse(date, DATE), TestWorld.current().processedDate);
    }

    @Then("số tiền hoàn lại là {double} và tồn kho giữ chỗ được nhả là {word}")
    @Then("số tiền hoàn lại là {double} và tồn kho giữ chỗ được nhả là {string}")
    public void refundAndStock(double refund, String released) {
        var world = TestWorld.current();
        assertEquals(refund, world.refundAmount);
        assertEquals(Boolean.parseBoolean(released), world.stockReleased);
    }

    @Then("lý do nghiệp vụ là {word}")
    @Then("lý do nghiệp vụ là {string}")
    public void businessReason(String reason) {
        assertNotNull(reason);
        assertFalse(reason.trim().isEmpty());
    }
}
