package fi.aalto.cs.apluscourses.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class APlusLocalizationUtilTest {

  @Test
  public void testGetEnglishName() {
    String multilingualName = "|fi:hei|en:hello|se:hej|";
    Assert.assertEquals("Correctly parses the English name", "hello",
        APlusLocalizationUtil.getEnglishName(multilingualName));
  }

  @Test
  public void testGetEnglishNameWithoutLocalization() {
    String name = "abcdefg";
    Assert.assertEquals("Returns the given string when the string doesn't contain localization",
        "abcdefg", APlusLocalizationUtil.getEnglishName(name));
  }

  @Test
  public void testLanguageCodeToName() {
    Assert.assertEquals("Finnish", APlusLocalizationUtil.languageCodeToName("fi"));
    Assert.assertEquals("English", APlusLocalizationUtil.languageCodeToName("en"));
    Assert.assertEquals("qwerty", APlusLocalizationUtil.languageCodeToName("qwerty"));
  }

}
