package fi.aalto.cs.intellij.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import fi.aalto.cs.intellij.PluginsTestHelper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class RequiredPluginsCheckerUtilTest extends PluginsTestHelper {

  @Test
  public void testIsPluginMissingOrDisabledWithActivePluginIdReturnsFalse(){
    String scalaId = "com.intellij";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertFalse(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithDisabledPluginIdReturnsTrue(){
    String scalaId = "org.jetbrains.android";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertTrue(result);
  }

  @Test
  public void testIsPluginMissingOrDisabledWithMissingPluginIdReturnsTrue(){
    String scalaId = "org.intellij.scala";

    boolean result = RequiredPluginsCheckerUtil.isPluginMissingOrDisabled(scalaId);

    assertTrue(result);
  }

  @Test
  public void testGetPluginsNamesStringWithEmptyInputReturnsEmpty() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("", result);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void testGetPluginsNamesStringWithNullInput() {
//    RequiredPluginsCheckerUtil.getPluginsNamesString(null);
//  }

  @Test
  public void testGetPluginsNamesStringWithFaultyPluginInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    validList.add(null);
    validList.addAll(getDummyPluginsListOfTwo());
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("A+ Courses, Scala", result);
  }

  @Test
  public void testGetPluginsNamesStringWithValidInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = getDummyPluginsListOfTwo();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    assertEquals("A+ Courses, Scala", result);
  }
}