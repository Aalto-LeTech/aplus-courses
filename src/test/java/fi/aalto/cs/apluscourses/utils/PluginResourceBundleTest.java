package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import org.junit.Test;

public class PluginResourceBundleTest {

  @Test
  public void testGetSimpleProperty() {
    PluginResourceBundle pluginResourceBundle = new PluginResourceBundle();
    String actual = pluginResourceBundle.bundle.getString("test.single");
    assertEquals("The retrieved bundle value is correct.", "blaaaah!", actual);
  }

  @Test
  public void testGetCombinedProperty() {
    PluginResourceBundle pluginResourceBundle = new PluginResourceBundle();
    String actual = MessageFormat.format(pluginResourceBundle.bundle.getString(
        "test.compound"),
        "blaaaah!", "blaaaah!");
    assertEquals("The retrieved bundle value is correct.",
        "blaaaah! blaaaah! blaaaah!", actual);
  }
}