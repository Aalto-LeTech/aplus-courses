package fi.aalto.cs.apluscourses.utils

import org.apache.commons.lang3.StringUtils
import org.jetbrains.annotations.NonNls

object APlusLocalizationUtil {
    private fun extractLocalizedText(localizedString: String, languageCode: String): String {
        return StringUtils.substringBetween(localizedString, "|$languageCode:", "|")
    }

    /**
     * Parses the given localized string and returns the text corresponding to the specified language.
     * If the entry in the requested language does not exist, the method attempts to localize the string in English.
     * If that fails too, the whole raw string is returned.
     * An example of a valid localized string is `"|en:hello|fi:terve|"`.
     *
     * @param localizedString The input string, which may be localized or not.
     * @param languageCode The requested language code, e.g. "fi" or "en".
     */
    fun getLocalizedName(localizedString: String, languageCode: String): String =
        if (localizedString.contains(languageCode))
            extractLocalizedText(localizedString, languageCode)
        else
            extractLocalizedText(localizedString, "en")

    /**
     * Returns the language name corresponding to the given ISO 639-1 language code. Only a few common
     * ones are supported, otherwise the given language code is returned.
     */
    @NonNls
    fun languageCodeToName(languageCode: String): String {
        // Hard-coded common language codes
        return when (languageCode) {
            "fi" -> "Finnish"
            "sv" -> "Swedish"
            "en" -> "English"
            else -> languageCode
        }
    }
}
