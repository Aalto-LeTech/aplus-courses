package fi.aalto.cs.intellij.common;

import java.io.ByteArrayInputStream;
import org.junit.Assert;
import org.junit.Test;

public class BuildInfoTest {

  @Test
  public void testCreateBuildInfoFromResources() {
    Resources res = new Resources(name -> {
      Assert.assertEquals("build-info.properties", name);
      return new ByteArrayInputStream("version=1.5.18\n".getBytes());
    });

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("1.5.18", buildInfo.version.toString());
  }

  @Test
  public void testCreateBuildInfoFromResourcesMissing() {
    Resources res = new Resources(name -> null);

    BuildInfo buildInfo = new BuildInfo(res);

    Assert.assertEquals("0.0.0", buildInfo.version.toString());
  }
}
