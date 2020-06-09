package fi.aalto.cs.apluscourses.integration;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EnvironmentChecker implements TestRule {

  private boolean isGivenEnvironment;

  /**
   * A JUnit rule that checks the environment.
   *
   * @param environmentName the {@link String} name of the corresponding environment variable to
   *                        check (true or false)
   */
  public EnvironmentChecker(String environmentName) {
    String getenv = System.getenv(environmentName);
    isGivenEnvironment = Boolean.parseBoolean(getenv);
    System.out.println("inside CI? " + isGivenEnvironment);
  }

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        if (isGivenEnvironment) {
          throw new AssumptionViolatedException("inside CI");
//          statement.evaluate();
        }
      }
    };
  }
}
