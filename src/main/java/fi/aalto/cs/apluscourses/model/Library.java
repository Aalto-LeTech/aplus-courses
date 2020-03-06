package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class Library {
  @NotNull
  private final String name;

  public Library(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
