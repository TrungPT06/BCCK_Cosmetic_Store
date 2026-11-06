package starter.stepdefinitions;

import io.cucumber.java.en.Then;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared step definitions for step patterns used across multiple features.
 * Avoids DuplicateStepDefinitionException by defining each shared pattern once.
 */
public class SharedStepDefinitions {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Then("T được thông báo lỗi là {word} với màu red")
    public void failure(String error) {
        var world = TestWorld.current();
        assertFalse(world.success);
        assertEquals(error, world.message);
        assertEquals("red", world.color);
    }

    @Then("đơn hàng được xử lý vào ngày {word}")
    public void processedDate(String date) {
        assertEquals(LocalDate.parse(date, DATE), TestWorld.current().processedDate);
    }

    @Then("số tiền hoàn lại là {double} và tồn kho giữ chỗ được nhả là {word}")
    public void refundAndStock(double refund, String released) {
        var world = TestWorld.current();
        assertEquals(refund, world.refundAmount);
        assertEquals(Boolean.parseBoolean(released), world.stockReleased);
    }
}
