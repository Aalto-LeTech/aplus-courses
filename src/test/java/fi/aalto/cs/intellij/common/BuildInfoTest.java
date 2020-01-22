package fi.aalto.cs.intellij.common;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Properties;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void testCreateBuildInfoFromProperties() throws BuildInfo.BuildInfoException {
    String versionString = "1.5.18";

    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, versionString);

    BuildInfo buildInfo = new BuildInfo(props);

    assertEquals(versionString, buildInfo.version.toString());
  }

  @Test
  public void testCreateBuildInfoFromIncompleteProperties() {
    Properties props = new Properties();
    props.setProperty("not-version-info", "1.2.3");

    try {
      new BuildInfo(props);
    } catch (BuildInfo.BuildInfoException ex) {
      assertThat(ex.getCause(), instanceOf(PropertiesReader.PropertyException.class));
      return;
    }
    fail();
  }

  @Test
  public void testCreateBuildInfoFromInvalidProperties() {
    Properties props = new Properties();
    props.setProperty(BuildInfo.PropertyKeys.VERSION, "invalid.version.string");

    try {
      new BuildInfo(props);
    } catch (BuildInfo.BuildInfoException ex) {
      assertThat(ex.getCause(), instanceOf(RuntimeException.class));
      return;
    }
    fail();
  }
}
