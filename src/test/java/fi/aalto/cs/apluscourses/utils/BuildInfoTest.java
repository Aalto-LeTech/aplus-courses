package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.junit.jupiter.api.Test;

public class BuildInfoTest {

  @Test
  public void testCreateBuildInfoFromProperties() throws PropertyException {
    String versionString = "1.5";
    String courseVersionString = "3.3";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, versionString);
    props.setProperty(BuildInfo.PropertyKeys.COURSE_VERSION, courseVersionString);

    BuildInfo buildInfo = new BuildInfo(props);

    assertEquals("Build info should have version given in properties.",
        versionString, buildInfo.pluginVersion.toString());

    assertEquals("Build info should have course version given in properties.",
        courseVersionString, buildInfo.courseVersion.toString());
  }

  @Test
  public void testCreateBuildInfoFromIncompleteProperties() {
    Properties props = new Properties();

    try {
      new BuildInfo(props);
    } catch (PropertyException ex) {
      assertEquals("The property key of exception should be one that is missing.",
          BuildInfo.PropertyKeys.VERSION, ex.getPropertyKey());
      return;
    }
    fail("Constructor should throw exception if properties do not contain necessary data.");
  }

  @Test
  public void testCreateBuildInfoFromInvalidProperties() {
    String invalidVersionString = "invalid.version.string";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, invalidVersionString);

    try {
      new BuildInfo(props);
    } catch (PropertyException ex) {
      assertEquals(BuildInfo.PropertyKeys.VERSION, ex.getPropertyKey());
      assertThat(ex.getCause(), instanceOf(Version.InvalidVersionStringException.class));
      assertThat(ex.getMessage(), containsString(invalidVersionString));
      return;
    }
    fail("Constructor should throw a PropertyException if the data is invalid.");
  }
}
