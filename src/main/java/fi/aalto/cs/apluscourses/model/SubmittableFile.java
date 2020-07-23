package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class SubmittableFile {
  @NotNull
  private final String key;
  @NotNull
  private final String name;

  public SubmittableFile(@NotNull String key, @NotNull String name) {
    this.key = key;
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String getKey() {
    return key;
  }
}
