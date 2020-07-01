package fi.aalto.cs.apluscourses.model;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class Submission {

  private final SubmittableExercise exercise;
  private final Path[] filePaths;
  @NotNull
  private final APlusAuthentication authentication;
  private final Group group;

  /**
   * Constructs a new instance.
   *
   * @param exercise Exercise.
   * @param filePaths Array of paths.
   * @param authentication API authentication.
   * @param group Group in which the submission is made.
   */
  public Submission(@NotNull SubmittableExercise exercise,
                    @NotNull Path[] filePaths,
                    @NotNull APlusAuthentication authentication,
                    @NotNull Group group) {
    this.exercise = exercise;
    this.filePaths = filePaths;
    this.authentication = authentication;
    this.group = group;
  }

  public void submit() {

  }
}
