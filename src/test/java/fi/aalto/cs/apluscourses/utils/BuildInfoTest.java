package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildInfoTest {

  @Test
  void testCreateBuildInfoFromProperties() throws PropertyException {
    String versionString = "1.5";
    String courseVersionString = "3.3";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, versionString);
    props.setProperty(BuildInfo.PropertyKeys.COURSE_VERSION, courseVersionString);

    BuildInfo buildInfo = new BuildInfo(props);

    Assertions.assertEquals(versionString, buildInfo.pluginVersion.toString(),
        "Build info should have version given in properties.");

    Assertions.assertEquals(courseVersionString, buildInfo.courseVersion.toString(),
        "Build info should have course version given in properties.");
  }

  @Test
  void testCreateBuildInfoFromIncompleteProperties() {
    Properties props = new Properties();

    try {
      new BuildInfo(props);
    } catch (PropertyException ex) {
      Assertions.assertEquals(BuildInfo.PropertyKeys.VERSION, ex.getPropertyKey(),
          "The property key of exception should be one that is missing.");
      return;
    }
    Assertions.fail("Constructor should throw exception if properties do not contain necessary data.");
  }

  @Test
  void testCreateBuildInfoFromInvalidProperties() {
    String invalidVersionString = "invalid.version.string";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, invalidVersionString);

    try {
      new BuildInfo(props);
    } catch (PropertyException ex) {
      Assertions.assertEquals(BuildInfo.PropertyKeys.VERSION, ex.getPropertyKey());
      MatcherAssert.assertThat(ex.getCause(), instanceOf(Version.InvalidVersionStringException.class));
      MatcherAssert.assertThat(ex.getMessage(), containsString(invalidVersionString));
      return;
    }
    Assertions.fail("Constructor should throw a PropertyException if the data is invalid.");
  }
}
