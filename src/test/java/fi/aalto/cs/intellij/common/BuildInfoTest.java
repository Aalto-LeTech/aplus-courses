package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Properties;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void testCreateBuildInfoFromProperties() throws PropertyException {
    String versionString = "1.5.18";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, versionString);

    BuildInfo buildInfo = new BuildInfo(props);

    assertEquals(versionString, buildInfo.version.toString());
  }

  @Test
  public void testCreateBuildInfoFromIncompleteProperties() {
    Properties props = new Properties();

    try {
      new BuildInfo(props);
    } catch (PropertyException ex) {
      assertEquals(BuildInfo.PropertyKeys.VERSION, ex.getPropertyKey());
      return;
    }
    fail();
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
    fail();
  }
}
