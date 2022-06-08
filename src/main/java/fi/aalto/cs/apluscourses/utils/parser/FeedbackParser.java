package fi.aalto.cs.apluscourses.utils.parser;

import org.jetbrains.annotations.NotNull;

public class FeedbackParser {

  @NotNull
  public FeedbackParser.TestResults parseTestResults(@NotNull String htmlString) {
    return new TestResults(-1, -1);
  }

  public static class TestResults {
    public final int succeeded;
    public final int failed;

    public TestResults(int succeeded, int failed) {
      this.succeeded = succeeded;
      this.failed = failed;
    }
  }
}
