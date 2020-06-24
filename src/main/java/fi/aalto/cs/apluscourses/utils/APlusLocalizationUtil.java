package fi.aalto.cs.apluscourses.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class APlusLocalizationUtil {

  /**
   * Parses the English name from the given string, or returns the string if the string contains
   * no localization. An example of a valid string is {@code "|en:hello|fi:terve|"}.
   *
   * @param name A string with a name in multiple languages, or just a single name.
   */
  @NotNull
  public static String getEnglishName(@NotNull String name) {
    String englishName = StringUtils.substringBetween(name, "|en:", "|");
    if (englishName != null) {
      return englishName;
    } else {
      // The name seems to not contain localization
      return name;
    }
  }

  private APlusLocalizationUtil() {

  }

}
