package fi.aalto.cs.apluscourses.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class APlusLocalizationUtilTest {

  @Test
  void testGetLocalizedName() {
    String multilingualName = "|fi:hei|en:hello|se:och samma på svenska|";
    Assertions.assertEquals("hei", APlusLocalizationUtil.getLocalizedName(multilingualName, "fi"),
        "Correctly parses the localized name in Finnish");
    Assertions.assertEquals("hello", APlusLocalizationUtil.getLocalizedName(multilingualName, "en"),
        "Correctly parses the localized name in English");
    Assertions.assertEquals("och samma på svenska", APlusLocalizationUtil.getLocalizedName(multilingualName, "se"),
        "Correctly parses the localized name in Swedish");
    Assertions.assertEquals("hello", APlusLocalizationUtil.getLocalizedName(multilingualName, "ee"),
        "Correctly returns the English text if the request language is unavailable");

    String nonEnglishText = "|fi:antauduttuaan|";
    Assertions.assertEquals("antauduttuaan", APlusLocalizationUtil.getLocalizedName(nonEnglishText, "fi"),
        "Correctly parses the localized name in Finnish");
    Assertions.assertEquals(nonEnglishText, APlusLocalizationUtil.getLocalizedName(nonEnglishText, "lv"),
        "Correctly returns the whole text if neither the requested nor English versions are available");

    Assertions.assertEquals("xyz", APlusLocalizationUtil.getLocalizedName("xyz", "en"),
        "Correctly returns the whole text if there is no localization information");
  }

  @Test
  void testLanguageCodeToName() {
    Assertions.assertEquals("Finnish", APlusLocalizationUtil.languageCodeToName("fi"));
    Assertions.assertEquals("English", APlusLocalizationUtil.languageCodeToName("en"));
    Assertions.assertEquals("qwerty", APlusLocalizationUtil.languageCodeToName("qwerty"));
  }

}
