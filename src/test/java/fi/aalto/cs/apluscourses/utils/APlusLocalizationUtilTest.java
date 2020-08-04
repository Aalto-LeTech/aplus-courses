package fi.aalto.cs.apluscourses.utils;

import junit.framework.Assert;
import org.junit.Test;

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

}
