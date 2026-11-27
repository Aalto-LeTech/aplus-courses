package fi.aalto.cs.apluscourses.utils

import org.jetbrains.annotations.NonNls

object APlusLocalizationUtil {
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
