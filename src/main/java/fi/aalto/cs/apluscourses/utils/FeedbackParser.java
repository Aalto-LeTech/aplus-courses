package fi.aalto.cs.apluscourses.utils;

import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedbackParser {

  /**
   * Returns the number in front of testType.
   * @param feedback Feedback for an assignment, containing test results
   * @param testType The type of test result (e.g. succeeded)
   * @return Number of tests for a given type
   */
  private static Optional<Integer> testsAmount(String feedback, String testType) {
    // O1_SPECIFIC
    String pattern = String.format("\\d+(?= +%s)", testType);
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(feedback);
    return m.find()
        ? Optional.of(Integer.parseInt(m.group(0)))
        : feedback.contains("could not be executed") ? Optional.of(-1) : Optional.empty();
  }

  /**
   * Parses the feedback for the test results.
   * @param feedback Feedback for an assignment, containing test results
   * @return An optional map, containing test results.
   */
  public static Optional<HashMap<String, Integer>> testResultsMap(String feedback) {
    // O1_SPECIFIC
    Optional<Integer> succeeded = testsAmount(feedback, "succeeded");
    Optional<Integer> failed    = testsAmount(feedback, "failed");
    Optional<Integer> canceled  = testsAmount(feedback, "canceled");

    if (!succeeded.isPresent() || !failed.isPresent() || !canceled.isPresent()) {
      return Optional.empty();
    }

    HashMap<String, Integer> results = new HashMap<>();
    results.put("succeeded", succeeded.get());
    results.put("failed", failed.get());
    results.put("canceled", canceled.get());

    return Optional.of(results);
  }

  /**
   * Turns feedback results into a string.
   * @param results Map of the results.
   * @return A string, telling if the tests passed.
   */
  public static String testResultString(HashMap<String, Integer> results) {
    int succeeded = results.get("succeeded");
    int failed    = results.get("failed");
    int canceled  = results.get("canceled");

    if (succeeded == -1) {
      return PluginResourceBundle.getText("presentation.optionalExercises.failedAll");
    }

    int totalFailed = failed + canceled;
    int totalTests  = succeeded + totalFailed;

    String plural = totalFailed == 1 ? "" : "s";

    return succeeded == totalTests
        ? PluginResourceBundle.getText("presentation.optionalExercises.passed")
        : PluginResourceBundle.getAndReplaceText(
                "presentation.optionalExercises.failed", totalFailed, plural);
  }
}
