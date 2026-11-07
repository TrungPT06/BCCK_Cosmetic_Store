package starter.stepdefinitions;

import io.cucumber.java.en.Then;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Các định nghĩa bước dùng chung cho các feature Cucumber.
 */
public class SharedStepDefinitions {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Then("T được thông báo lỗi là {word}")
    @Then("T được thông báo lỗi là {string}")
    public void failure(String expectedError) {
        TestWorld world = TestWorld.current();
        assertFalse(world.success, "Kỳ vọng thất bại nhưng lại thành công");
        assertEquals(expectedError, world.message);
    }

    @Then("đơn hàng được xử lý vào ngày {word}")
    @Then("đơn hàng được xử lý vào ngày {string}")
    public void processedDate(String expectedDateStr) {
        LocalDate expectedDate = LocalDate.parse(expectedDateStr, DATE_FORMAT);
        assertEquals(expectedDate, TestWorld.current().processedDate, "Ngày xử lý trong Response không khớp");
    }

    @Then("số tiền hoàn lại là {long} và tồn kho giữ chỗ được nhả là {word}")
    @Then("số tiền hoàn lại là {long} và tồn kho giữ chỗ được nhả là {string}")
    public void refundAndStock(long expectedRefund, String stockReleasedStr) {
        TestWorld world = TestWorld.current();
        assertEquals(expectedRefund, world.refundAmount);
        assertEquals(Boolean.parseBoolean(stockReleasedStr), world.stockReleased);
    }
}
