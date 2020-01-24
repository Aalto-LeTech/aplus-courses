package fi.aalto.cs.intellij.utils;

import static fi.aalto.cs.intellij.PluginsTestHelper.getDummyPluginsListOfTwo;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RequiredPluginsCheckerUtilTest {

  @Test
  public void testGetPluginsNamesStringWithEmptyInputReturnsEmpty() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    Assert.assertEquals("", result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetPluginsNamesStringWithNullInput() {
    RequiredPluginsCheckerUtil.getPluginsNamesString(null);
  }

  @Test
  public void testGetPluginsNamesStringWithFaultyPluginInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = new ArrayList<>();
    validList.add(null);
    validList.addAll(getDummyPluginsListOfTwo());
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    Assert.assertEquals("A+ Courses, Scala", result);
  }

  @Test
  public void testGetPluginsNamesStringWithValidInputReturnsRightResult() {
    List<IdeaPluginDescriptor> validList = getDummyPluginsListOfTwo();
    String result = RequiredPluginsCheckerUtil.getPluginsNamesString(validList);

    Assert.assertEquals("A+ Courses, Scala", result);
  }
}