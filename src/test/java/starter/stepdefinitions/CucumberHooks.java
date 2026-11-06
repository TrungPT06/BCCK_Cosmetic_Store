package starter.stepdefinitions;

import io.cucumber.java.Before;

/**
 * Cucumber hooks – runs before each scenario to reset shared state.
 */
public class CucumberHooks {
    @Before
    public void resetWorld() {
        TestWorld.reset();
    }
}

