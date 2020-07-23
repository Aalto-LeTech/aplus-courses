package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Submission {

  @NotNull
  private final Exercise exercise;
  @NotNull
  private final SubmissionInfo submissionInfo;
  @NotNull
  private final Map<String, Path> files;
  @NotNull
  private final Group group;

  /**
   * Constructs a new object instance.
   *
   * @param exercise       Exercise.
   * @param submissionInfo Information for the submission.
   * @param files          Map from keys to file paths.
   * @param group          Group in which the submission is made.
   */
  public Submission(@NotNull Exercise exercise,
                    @NotNull SubmissionInfo submissionInfo,
                    @NotNull Map<String, Path> files,
                    @NotNull Group group) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.files = files;
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
  public Map<String, Path> getFiles() {
    return files;
  }
}
