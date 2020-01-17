package fi.aalto.cs.intellij.common;

import java.util.Properties;

public class BuildInfo {

  private static String RESOURCE_NAME = "build-info";
  private static BuildInfo _instance;

  private String version;

  private static BuildInfo initialize(Resources resources) {
    return new BuildInfo(resources.getPropertiesFromResource(RESOURCE_NAME));
  }

  BuildInfo(Properties props) {
    version = props.getProperty("version");
  }
}
