package fi.aalto.cs.apluscourses.utils;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class APlusLocalizationUtil {

  private static String extractLocalizedText(@NotNull String localizedString, @NotNull String languageCode) {
    return StringUtils.substringBetween(localizedString, "|" + languageCode + ":", "|");
  }

  /**
   * Parses the given localized string and returns the text corresponding to the specified language.
   * If the entry in the requested language does not exist, the method attempts to localize the string in English.
   * If that fails too, the whole raw string is returned.
   * An example of a valid localized string is {@code "|en:hello|fi:terve|"}.
   *
   * @param localizedString The input string, which may be localized or not.
   * @param languageCode The requested language code, e.g. "fi" or "en".
   */
  @NotNull
  public static String getLocalizedName(@NotNull String localizedString, @NotNull String languageCode) {
    return Objects.requireNonNullElse(extractLocalizedText(localizedString, languageCode),
        Objects.requireNonNullElse(extractLocalizedText(localizedString, "en"), localizedString));
  }

  /**
   * Returns the language name corresponding to the given ISO 639-1 language code. Only a few common
   * ones are supported, otherwise the given language code is returned.
   */
  @NotNull
  public static String languageCodeToName(@NotNull String languageCode) {
    // Hard-coded common language codes
    switch (languageCode) {
      case "fi":
        return "Finnish";
      case "sv":
        return "Swedish";
      case "en":
        return "English";
      default:
        return languageCode;
    }
  }

  private APlusLocalizationUtil() {

  }

}
