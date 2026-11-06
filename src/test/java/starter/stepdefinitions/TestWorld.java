package starter.stepdefinitions;

import java.time.LocalDate;

/**
 * Scenario-scoped shared state between step definition classes.
 * Uses a ThreadLocal-backed singleton so that Serenity's SerenityObjectFactory
 * (which requires no-arg constructors) can still share state across step classes.
 *
 * Each scenario resets via the @Before hook in CucumberHooks.
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
