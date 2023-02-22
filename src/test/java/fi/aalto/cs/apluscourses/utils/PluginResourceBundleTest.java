package fi.aalto.cs.apluscourses.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PluginResourceBundleTest {

  @Test
  void testGetSimpleProperty() {
    String actual = PluginResourceBundle.getText("test.single");
    Assertions.assertEquals("blaaaah!", actual, "The retrieved bundle value is correct.");
  }

  @Test
  void testGetCombinedProperty() {
    String actual = PluginResourceBundle.getAndReplaceText("test.compound",
        "blaaaah!", "blaaaah!");
    Assertions.assertEquals("blaaaah! blaaaah! blaaaah!", actual, "The retrieved bundle value is correct.");
  }
}