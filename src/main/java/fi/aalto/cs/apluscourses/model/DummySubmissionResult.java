package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class DummySubmissionResult extends SubmissionResult {
  public DummySubmissionResult(long submissionId, @NotNull Exercise exercise) {
    super(submissionId, 0, 0.0, Status.GRADED, exercise);
  }
}
