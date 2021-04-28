package fi.aalto.cs.apluscourses.model.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Arguments {
  @Nullable String get(@NotNull String key);

  /**
   * Gets an argument or throws a runtime exception if the argument is missing.
   * @param key Key
   * @return The (non-null) value of the argument.
   */
  default @NotNull String getOrThrow(@NotNull String key) {
    String value = get(key);
    if (value == null) {
      throw new IllegalArgumentException("Argument missing: " + key);
    }
    return value;
  }

  static @NotNull Arguments empty() {
    return key -> null;
  }
}
