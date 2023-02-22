package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;

public class PluginDependency {

  private final @NotNull String displayName;

  private final @NotNull String id;

  public PluginDependency(@NotNull String displayName, @NotNull String id) {
    this.displayName = displayName;
    this.id = id;
  }

  public @NotNull String getDisplayName() {
    return displayName;
  }

  public @NotNull String getId() {
    return id;
  }
}
