package starter;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Cucumber Test Suite - BDD vòng ngoài (Acceptance Tests).
 *
 * Chạy tất cả feature files trong thư mục resources/features/
 * với step definitions từ package starter.stepdefinitions.
 *
 * Kết hợp với Serenity BDD để tạo Living Documentation.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "starter.stepdefinitions")
public class CucumberTestSuite {
}

