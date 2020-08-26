package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.Nullable;

public class StringUtil {
  private StringUtil() {

  }

  public static boolean isNullOrBlank(@Nullable String string) {
    return string == null || string.trim().isEmpty();
  }
}
