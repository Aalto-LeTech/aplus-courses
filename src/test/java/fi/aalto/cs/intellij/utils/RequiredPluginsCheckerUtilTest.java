package fi.aalto.cs.intellij.utils;

import static fi.aalto.cs.intellij.PluginsTestHelper.getDummyPluginsListOfTwo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class RequiredPluginsCheckerUtilTest {

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
            + " the dummy plugins and not the 'null'.", "A+ Courses, Scala", result);
  }

  @Test
  public void testGetPluginsNamesStringWithValidInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = getDummyPluginsListOfTwo();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("The result of provision of a dummy list should contain names the "
        + "dummy plugins.", "A+ Courses, Scala", result);
  }
}