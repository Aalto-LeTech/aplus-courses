package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.TestHelper.getDummyPluginsListOfTwo;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class RequiredPluginsCheckerUtilTest extends BasePlatformTestCase implements TestHelper {

  public static final String THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH =
      "The resulting data structure is of a proper length.";

  @Test
  public void testIsEntryContentsNonNullWithNullInputValueReturnsFalse() {
    Map<String, String> input = new HashMap<>();
    input.put("validKey", null);

    assertFalse("Ensure the 'false' result for 'null'-key containing Map.",
        RequiredPluginsCheckerUtil.isEntryContentsNonNull(input.entrySet().iterator().next()));
  }

  @Test
  public void testIsEntryContentsNonNullWithNullKeyInputReturnsFalse() {
    Map<String, String> input = new HashMap<>();
    input.put(null, "validValue");

    assertFalse("Ensure the 'false' result for 'null'-value containing Map.",
        RequiredPluginsCheckerUtil.isEntryContentsNonNull(input.entrySet().iterator().next()));
  }

  @Test
  public void testIsEntryContentsNonNullWithValiEntryReturnsFalse() {
    Map<String, String> input = new HashMap<>();
    input.put("validKey", "validValue");

    assertTrue("Ensure the 'true' result for a valid entry.",
        RequiredPluginsCheckerUtil.isEntryContentsNonNull(input.entrySet().iterator().next()));
  }

  @Test
  public void testGetPluginsNamesStringWithEmptyInputReturnsEmpty() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("The result of provision of an empty list should be empty. ",
        "", result);
  }

  @Test
  public void testGetPluginsNamesStringWithFaultyPluginInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    validList.add(null);
    validList.addAll(getDummyPluginsListOfTwo());
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals(
        "The result of provision of a dummy list containing 'null' should contain names of"
            + " the dummy plugins and not the 'null'.", "A+ Courses, Liferay IntelliJ "
            + "Plugin", result);
  }

  @Test
  public void testGetPluginsNamesStringWithValidInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = getDummyPluginsListOfTwo();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("The result of provision of a dummy list should contain names the "
        + "dummy plugins.", "A+ Courses, Liferay IntelliJ Plugin", result);
  }

  @Test
  public void testFilterMissingOrDisabledPluginNamesWithCorrectInputWorks() {
    Map<String, String> requiredPluginNames = new HashMap<>();
    getDummyPluginsListOfTwo().forEach(descriptor ->
        requiredPluginNames.put(descriptor.getPluginId().getIdString(), descriptor.getName()));

    Map<String, String> result =
        RequiredPluginsCheckerUtil.filterMissingOrDisabledPluginNames(requiredPluginNames);
    assertEquals(THE_RESULTING_DATA_STRUCTURE_IS_OF_A_PROPER_LENGTH,
        1, result.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilterMissingOrDisabledPluginNamesWorksWithCorrectInputWithNullWorks() {
    Map<String, String> requiredPluginNames = new HashMap<>();
    getDummyPluginsListOfTwo().forEach(descriptor ->
        requiredPluginNames.put(descriptor.getPluginId().getIdString(), descriptor.getName()));
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
    PluginManager.getPlugin(PluginId.getId(scalaPluginId)).setEnabled(false);

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaPluginId);

    assertTrue("The predicate is 'true' for not installed plugin.", result);
  }

  @Test
  public void testGetAvailablePluginsFromMainRepoIsNotEmpty() {
    List<IdeaPluginDescriptor> availablePlugins = RequiredPluginsCheckerUtil
        .getAvailablePluginsFromMainRepo();

    assertNotEmpty(availablePlugins);
    assertTrue("Amount of available plugins should be substantial.",
        2000 < availablePlugins.size());
  }
}
