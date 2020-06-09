package fi.aalto.cs.apluscourses.integration;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EnvironmentChecker implements TestRule {

  private boolean isGivenEnvironment;

  public EnvironmentChecker(String environmentName) {
    String getenv = System.getenv(environmentName);
    isGivenEnvironment = Boolean.parseBoolean(getenv);
    System.out.println("CI env var is: " + isGivenEnvironment);
  }

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        if (isGivenEnvironment) {
          statement.evaluate();
        }
      }
    };
  }
}
