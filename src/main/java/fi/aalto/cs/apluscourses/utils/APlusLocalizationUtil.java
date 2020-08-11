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

  /**
   * Returns the language name corresponding to the given ISO 639-1 language code. Only a few common
   * ones are supported, otherwise the given language code is returned.
   */
  @NotNull
  public static String languageCodeToName(@NotNull String languageCode) {
    // Hard-coded common language codes
    if ("fi".equals(languageCode)) {
      return "Finnish";
    } else if ("sv".equals(languageCode)) {
      return "Swedish";
    } else if ("en".equals(languageCode)) {
      return "English";
    } else {
      return languageCode;
    }
  }

  private APlusLocalizationUtil() {

  }

}
