package fi.aalto.cs.apluscourses.model.task;

import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

@FunctionalInterface
public interface Arguments {
  @Nullable Object opt(@NotNull String key);

  /**
   * Gets an argument or throws a runtime exception if the argument is missing.
   *
   * @param key Key
   * @return The (non-null) value of the argument.
   */
  default @NotNull String getString(@NotNull String key) {
    if (!(opt(key) instanceof String)) {
      throw new IllegalArgumentException("Argument is not a String: " + key);
    }
    String value = (String) opt(key);
    if (value == null) {
      throw new IllegalArgumentException("Argument missing: " + key);
    }
    return value;
  }

  /**
   * Gets an argument or throws a runtime exception if the argument is missing.
   *
   * @param key Key
   * @return The (non-null) array values of the argument.
   */
  default @NotNull String[] getArray(@NotNull String key) {
    if (!(opt(key) instanceof JSONArray)) {
      throw new IllegalArgumentException("Argument is not a JSONArray: " + key);
    }
    JSONArray jsonArray = (JSONArray) opt(key);
    if (jsonArray == null) {
      throw new IllegalArgumentException("Argument missing: " + key);
    }

    return JsonUtil.parseArray(jsonArray, JSONArray::getString, Function.identity(), String[]::new);
  }

  static @NotNull Arguments empty() {
    return key -> null;
  }
}
