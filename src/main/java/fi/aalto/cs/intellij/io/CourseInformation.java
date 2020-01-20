package fi.aalto.cs.intellij.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseInformation {
  private static final Logger logger = LoggerFactory
      .getLogger(CourseInformation.class);

  private static String courseName;
  private static Map<String, URL> moduleURLs;

  private CourseInformation() {
  }

  /**
   * Parses the given course configuration file.
   * @param pathToCourseConfig The path to the course configuration file.
   */
  public static void parse(String pathToCourseConfig) {
    courseName = "O1";
    moduleURLs = new HashMap<String, URL>();

    // TODO: actually read this stuff from the config file
    try {
      moduleURLs.put("O1Library", new URL("https://example.com"));
      moduleURLs.put("GoodStuff", new URL("https://example.com"));
    } catch (MalformedURLException e) {
      logger.error("Invalid URL in course configuration file: " + e.getMessage());
    }
  }

  /**
   * Returns the name of the currently loaded course.
   */
  public static String getCourseName() {
    return courseName;
  }

  /**
   * Returns the names of the code modules for the currently loaded course.
   */
  public static List<String> getModuleNames() {
    List<String> moduleNames = new ArrayList<String>();
    moduleNames.addAll(moduleURLs.keySet());
    return moduleNames;
  }

}
