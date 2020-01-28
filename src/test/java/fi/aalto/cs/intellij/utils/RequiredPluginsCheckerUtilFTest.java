package fi.aalto.cs.intellij.utils;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.Arrays;
import org.junit.Test;

public class RequiredPluginsCheckerUtilFTest extends PluginsTestHelper {

  @Test
  public void testFilterMissingOrDisabledPluginNames(){}


  @Test
  public void testIsPluginMissingOrDisabledWithActivePluginIdReturnsFalse() {
    String corePluginId = "com.intellij";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(corePluginId);

    assertFalse("The predicate is 'false' for installed and enabled plugin.", result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithDisabledPluginIdReturnsTrue() {
    Arrays.stream(PluginManager.getPlugins()).forEach(e -> System.out.println(e.getPluginId()));
    String corePluginId = "com.intellij";
    PluginManager.getPlugin(PluginId.getId(corePluginId)).setEnabled(false);

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(corePluginId);
    assertTrue("The predicate is 'true' for installed but disabled plugin.", result);

    /** Consider this as a side effects removal action. Lightweight tests use the same virtual
     * project, so this line is reactivating the core plugin.
     * @see <a href="https://www.jetbrains.org/intellij/sdk/docs/basics/testing_plugins
     * /light_and_heavy_tests.html">Light and Heavy Tests</a>
     */
    PluginManager.getPlugin(PluginId.getId(corePluginId)).setEnabled(true);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithMissingPluginIdReturnsTrue() {
    String scalaPluginId = "org.intellij.scala";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaPluginId);

    assertTrue("The predicate is 'true' for not installed plugin.", result);
  }
}
