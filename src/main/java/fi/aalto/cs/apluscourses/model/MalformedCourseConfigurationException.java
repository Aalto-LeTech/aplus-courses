package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MalformedCourseConfigurationException extends Exception {

  private static final long serialVersionUID = -2707324004141394128L;
  @NotNull
  private final String pathToConfigurationFile;

  public MalformedCourseConfigurationException(@NotNull String pathToConfigurationFile,
                                               @NotNull String message,
                                               @Nullable Throwable cause) {
    super(message, cause);
    this.pathToConfigurationFile = pathToConfigurationFile;
  }

  @NotNull
  public String getPathToConfigurationFile() {
    return pathToConfigurationFile;
  }
}
