package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;

import fi.aalto.cs.apluscourses.model.DummySubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.jetbrains.annotations.NotNull;

public class SubmissionResultUtil {
  private SubmissionResultUtil() {
  }

  /**
   * Creates a status text for a submission result.
   */
  @NotNull
  public static String getStatus(SubmissionResult submissionResult) {
    var testsFailed = submissionResult.getTestsFailed();
    var testsFailedResourceKey = testsFailed == 1 ? "presentation.submissionResultViewModel.testFailed" :
        "presentation.submissionResultViewModel.testsFailed";
    var testsFailedString = testsFailed < 1 ? "" : getAndReplaceText(testsFailedResourceKey, testsFailed);
    return submissionResult instanceof DummySubmissionResult ? "???"
        : getAndReplaceText("presentation.submissionResultViewModel.points",
        submissionResult.getPoints(), submissionResult.getExercise().getMaxPoints()) + testsFailedString;
  }
}
