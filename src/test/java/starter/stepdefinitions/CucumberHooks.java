package starter.stepdefinitions;

import io.cucumber.java.Before;

/**
 * Cucumber hooks – chạy trước mỗi kịch bản (scenario) để thiết lập lại trạng thái dùng chung.
 */
public class CucumberHooks {
    @Before
    public void resetWorld() {
        TestWorld.reset();
    }
}

