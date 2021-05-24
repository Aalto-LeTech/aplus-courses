package fi.aalto.cs.apluscourses.model.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

  /**
   * Gets an argument or throws a runtime exception if the argument is missing.
   * @param key Key
   * @return The (non-null) array values of the argument.
   */
  default @NotNull String[] getArrayOrThrow(@NotNull String key) {
    String value = get(key);
    if (value == null) {
      throw new IllegalArgumentException("Argument missing: " + key);
    }
    List<String> array = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(value, ",", false);
    while (tokenizer.hasMoreTokens()) {
      array.add(tokenizer.nextToken());
    }
    return array.toArray(new String[]{});
  }

  static @NotNull Arguments empty() {
    return key -> null;
  }
}
