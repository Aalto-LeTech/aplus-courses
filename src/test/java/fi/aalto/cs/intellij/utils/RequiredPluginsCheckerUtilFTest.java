package fi.aalto.cs.intellij.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class RequiredPluginsCheckerUtilFTest extends PluginsTestHelper {

  public static final String THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH =
      "The resulting data structure is of a proper length.";

  @Test
  public void testFilterMissingOrDisabledPluginNamesWithCorrectInputWorks() {
    Map<String, String> requiredPluginNames = new HashMap<>();
    getDummyPluginsListOfTwo().forEach(descriptor ->
        requiredPluginNames.put(descriptor.getName(),
        descriptor.getPluginId().getIdString()));

    Map<String, String> result =
        RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames(requiredPluginNames);
    assertEquals(THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH,
        1, result.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilterMissingOrDisabledPluginNamesWorksWithCorrectInputWithNullWorks() {
    Map<String, String> requiredPluginNames = new HashMap<>();
    getDummyPluginsListOfTwo().forEach(descriptor ->
        requiredPluginNames.put(descriptor.getName(),
        descriptor.getPluginId().getIdString()));
    requiredPluginNames.put(null, null);

    Map<String, String> result =
        RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames(requiredPluginNames);
    assertEquals(THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH,
        1, result.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilterMissingOrDisabledPluginNamesWorksWithEmptyInputWorks() {
    Map<String, String> requiredPluginNames = new HashMap<>();

    Map<String, String> result =
        RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames(requiredPluginNames);
    assertEquals(THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH,
        0, result.size());
  }

  @Test
  public void testIsPluginMissingOrDisabledWithActivePluginIdReturnsFalse() {
    String corePluginId = "com.intellij";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(corePluginId);

    assertFalse("The predicate is 'false' for installed and enabled plugin.", result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithDisabledPluginIdReturnsTrue() {
    String corePluginId = "com.intellij";
    PluginManager.getPlugin(PluginId.getId(corePluginId)).setEnabled(false);

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(corePluginId);
    assertTrue("The predicate is 'true' for installed but disabled plugin.", result);

    /** Consider this as a side effects removal action. Lightweight tests reuse the same virtual
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

  @Test
  public void testGetAvailablePluginsFromMainRepoIsNotEmpty() {
    List<IdeaPluginDescriptor> availablePlugins = RequiredPluginsCheckerUtil
        .getAvailablePluginsFromMainRepo();

    assertNotEmpty(availablePlugins);
    assertTrue("Amount of available plugins should be substantial.",
        3000 < availablePlugins.size());
  }
}
