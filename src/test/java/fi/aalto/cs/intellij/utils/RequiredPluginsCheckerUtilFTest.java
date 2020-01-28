package fi.aalto.cs.intellij.utils;

import fi.aalto.cs.intellij.PluginsTestHelper;
import org.junit.Test;

public class RequiredPluginsCheckerUtilFTest extends PluginsTestHelper {

  @Test
  public void testIsPluginMissingOrDisabledWithActivePluginIdReturnsFalse() {
    String corePluginId = "com.intellij";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(corePluginId);

    assertFalse(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithDisabledPluginIdReturnsTrue() {
    String androidPluginId = "org.jetbrains.android";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(androidPluginId);

    assertTrue(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithMissingPluginIdReturnsTrue() {
    String scalaPluginId = "org.intellij.scala";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaPluginId);

    assertTrue(result);
  }


}
