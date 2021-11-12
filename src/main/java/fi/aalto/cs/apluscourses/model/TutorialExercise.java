package fi.aalto.cs.apluscourses.model;

import java.util.OptionalLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TutorialExercise extends Exercise {

  private final @NotNull Tutorial tutorial;

  /**
   * A constructor.
   */
  public TutorialExercise(long id,
                          @NotNull String name,
                          @NotNull String htmlUrl,
                          @NotNull SubmissionInfo submissionInfo,
                          int maxPoints,
                          int maxSubmissions,
                          @NotNull OptionalLong bestSubmissionId,
                          @Nullable String difficulty,
                          @NotNull Tutorial tutorial) {
    super(id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions, bestSubmissionId, difficulty);
    this.tutorial = tutorial;
  }

  public @NotNull Tutorial getTutorial() {
    return tutorial;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(getId());
  }

  @Override
  public boolean equals(Object obj) {
    /* Honestly, this could be removed even if linters probably complained.
     * We want two exercises to be considered equal if their ids match no matter their types.  */
    return obj instanceof TutorialExercise && ((TutorialExercise) obj).getId() == getId();
  }

}
