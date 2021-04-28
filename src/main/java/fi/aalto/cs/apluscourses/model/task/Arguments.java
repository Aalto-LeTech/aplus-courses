package fi.aalto.cs.apluscourses.model.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Arguments {
  @Nullable String get(@NotNull String key);

  default @NotNull String getOrThrow(@NotNull String key) {
    String value = get(key);
    if (value == null) {
      throw new IllegalArgumentException("Argument missing: " + key);
    }
    return value;
  }
}
