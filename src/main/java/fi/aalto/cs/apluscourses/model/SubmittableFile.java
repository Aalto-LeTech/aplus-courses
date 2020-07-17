package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class SubmittableFile {
  @NotNull
  private final String name;

  public SubmittableFile(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
