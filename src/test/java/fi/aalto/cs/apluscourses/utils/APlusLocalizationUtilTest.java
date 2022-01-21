package fi.aalto.cs.apluscourses.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class APlusLocalizationUtilTest {

  @Test
  void testGetEnglishName() {
    String multilingualName = "|fi:hei|en:hello|se:hej|";
    Assertions.assertEquals("hello", APlusLocalizationUtil.getEnglishName(multilingualName),
        "Correctly parses the English name");
  }

  @Test
  void testGetEnglishNameWithoutLocalization() {
    String name = "abcdefg";
    Assertions.assertEquals("abcdefg", APlusLocalizationUtil.getEnglishName(name),
        "Returns the given string when the string doesn't contain localization");
  }

  @Test
  void testLanguageCodeToName() {
    Assertions.assertEquals("Finnish", APlusLocalizationUtil.languageCodeToName("fi"));
    Assertions.assertEquals("English", APlusLocalizationUtil.languageCodeToName("en"));
    Assertions.assertEquals("qwerty", APlusLocalizationUtil.languageCodeToName("qwerty"));
  }

}
