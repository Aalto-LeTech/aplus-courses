package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class Submission {

  @NotNull
  private final Exercise exercise;
  @NotNull
  private final SubmissionInfo submissionInfo;
  @NotNull
  private final Path[] filePaths;
  @NotNull
  private final Group group;

  /**
   * Constructs a new object instance.
   *
   * @param exercise       Exercise.
   * @param submissionInfo Information for the submission.
   * @param filePaths      Array of paths.
   * @param group          Group in which the submission is made.
   */
  public Submission(@NotNull Exercise exercise,
                    @NotNull SubmissionInfo submissionInfo,
                    @NotNull Path[] filePaths,
                    @NotNull Group group) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.filePaths = filePaths;
    this.group = group;
  }

  @NotNull
  public Group getGroup() {
    return group;
  }

  @NotNull
  public Exercise getExercise() {
    return exercise;
  }

  @NotNull
  public SubmissionInfo getSubmissionInfo() {
    return submissionInfo;
  }

  @NotNull
  public Path[] getFilePaths() {
    return filePaths;
  }
}
