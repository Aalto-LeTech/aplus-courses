package fi.aalto.cs.intellij.utils;

import fi.aalto.cs.intellij.PluginsTestHelper;
import org.junit.Test;

public class RequiredPluginsCheckerUtilFTest extends PluginsTestHelper {

  @Test
  public void testIsPluginMissingOrDisabledWithActivePluginIdReturnsFalse() {
    String scalaId = "com.intellij";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertFalse(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithDisabledPluginIdReturnsTrue() {
    String scalaId = "org.jetbrains.android";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertTrue(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithMissingPluginIdReturnsTrue() {
    String scalaId = "org.intellij.scala";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertTrue(result);
  }


}
