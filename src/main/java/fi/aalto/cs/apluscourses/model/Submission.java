package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.model.exercise.Exercise;
import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Submission {

  @NotNull
  private final Exercise exercise;
  @NotNull
  private final Map<String, Path> files;
  @NotNull
  private final Group group;
  @NotNull
  private final String language;

  /**
   * Constructs a new object instance.
   *
   * @param exercise Exercise.
   * @param files    Map from keys to file paths.
   * @param group    Group in which the submission is made.
   */
  public Submission(@NotNull Exercise exercise,
                    @NotNull Map<String, Path> files,
                    @NotNull Group group,
                    @NotNull String language) {
    this.exercise = exercise;
    this.files = files;
    this.group = group;
    this.language = language;
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
  public Map<String, Path> getFiles() {
    return files;
  }

  @NotNull
  public String getLanguage() {
    return language;
  }
}
