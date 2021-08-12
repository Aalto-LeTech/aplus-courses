package fi.aalto.cs.apluscourses.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APlusLogger {
  private APlusLogger() {

  }

  public static final Logger logger = LoggerFactory.getLogger("A+ Courses " + BuildInfo.INSTANCE.pluginVersion);
}
